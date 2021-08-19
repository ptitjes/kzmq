package org.zeromq

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SelectTests {
    @Test
    fun onReceiveTest() = runBlocking {
        val context = Context(JeroMQ)
        val data = (0 until 10).toList()

        val publisher1 = context.createPublisher()
        publisher1.bind("inproc://pub1")

        val publisher2 = context.createPublisher()
        publisher2.bind("inproc://pub2")

        val subscriber1 = context.createSubscriber()
        subscriber1.connect("inproc://pub1")
        subscriber1.subscribe("")

        val subscriber2 = context.createSubscriber()
        subscriber2.connect("inproc://pub2")
        subscriber2.subscribe("")

        val publication = launch {
            for (datum in data) {
                delay(100)
                val message = Message("$datum".encodeToByteArray())
                (if (datum % 2 == 0) publisher1 else publisher2).send(message)
            }
        }

        val subscription = launch {
            val received = mutableListOf<ByteArray>()
            repeat(10) {
                val async1 = async { subscriber1.receive() }
                val async2 = async { subscriber2.receive() }

                received += select<Message> {
//                    subscriber1.produceIn(this@runBlocking).onReceive { it }
//                    subscriber2.produceIn(this@runBlocking).onReceive { it }
                    async1.onAwait { it }
                    async2.onAwait { it }
                }.singleOrThrow()

                async1.cancel()
                async2.cancel()
            }

            assertContentEquals(data, received.map { it.decodeToString().toInt() }.sorted())
        }

        subscription.join()
        publication.join()
    }
}
