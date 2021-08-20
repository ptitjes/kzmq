package org.zeromq.tests

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.zeromq.Message
import org.zeromq.collectToSocket
import org.zeromq.consumeAsFlow
import org.zeromq.tests.utils.contextTests
import org.zeromq.tests.utils.randomAddress
import org.zeromq.tests.utils.test
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PubSubTests {

    @Test
    fun simpleTest() = contextTests {
        test { context ->
            val address = randomAddress()
            val sent = "Hello 0MQ!".encodeToByteArray()

            val publisher = context.createPublisher()
            publisher.bind(address)

            val subscriber = context.createSubscriber()
            subscriber.connect(address)
            subscriber.subscribe("")

            val subscription = launch {
                val received = subscriber.receive().singleOrThrow()
                assertEquals(sent, received)
            }

            val publication = launch {
                publisher.send(Message(sent))
            }

            subscription.join()
            publication.join()
        }
    }

    @Test
    fun flowTest() = contextTests {
        test { context ->
            val address = randomAddress()
            val testFlow = flow { for (i in 0..9) emit(i.toString().encodeToByteArray()) }

            val publisher = context.createPublisher()
            publisher.bind(address)

            val subscriber = context.createSubscriber()
            subscriber.connect(address)
            subscriber.subscribe("")

            val subscription = launch {
                val values = subscriber.consumeAsFlow().take(10).map { it.singleOrThrow() }

                assertContentEquals(
                    testFlow.map { it.decodeToString() }.toList(),
                    values.map { it.decodeToString() }.toList()
                )
            }

            val publication = launch {
                testFlow.map { Message(it) }.collectToSocket(publisher)
            }

            subscription.join()
            publication.join()
        }
    }

    @Test
    fun selectTest() = contextTests {
        test { context ->
            val address1 = randomAddress()
            val address2 = randomAddress()

            val data = (0 until 10).toList()

            val publisher1 = context.createPublisher()
            publisher1.bind(address1)

            val publisher2 = context.createPublisher()
            publisher2.bind(address2)

            val subscriber1 = context.createSubscriber()
            subscriber1.connect(address1)
            subscriber1.subscribe("")

            val subscriber2 = context.createSubscriber()
            subscriber2.connect(address2)
            subscriber2.subscribe("")

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

            val publication = launch {
                for (datum in data) {
                    delay(100)
                    val message = Message("$datum".encodeToByteArray())
                    (if (datum % 2 == 0) publisher1 else publisher2).send(message)
                }
            }

            subscription.join()
            publication.join()
        }
    }
}
