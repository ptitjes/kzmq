package org.zeromq

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
class Simple {

    @Test
    fun testSimple() = runBlockingTest {
        val context = Context(JS)
        val sent = "Hello 0MQ!"

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testSimple")

            delay(100)
            publisher.send(Message(sent.encodeToByteArray()))
        }

        val subscription = launch {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testSimple")
            subscriber.subscribe("")

            val received = subscriber.receive()
                .singleOrThrow().decodeToString()

            assertEquals(sent, received)
        }

        subscription.join()
        publication.join()
    }

    @Test
    fun testFlow() = runBlockingTest {
        val context = Context(JS)
        val intFlow: Flow<Int> = flow { for (i in 0..9) emit(i) }

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testFlow")

            delay(100)
            intFlow
                .map { Message(it.toString().encodeToByteArray()) }
                .collectToSocket(publisher)
        }

        val subscription = launch {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testFlow")
            subscriber.subscribe("")

            val values = subscriber.asFlow()
                .take(10)
                .map { it.singleOrThrow().decodeToString().toInt() }

            assertContentEquals(intFlow.toList(), values.toList())
        }

        subscription.join()
        publication.join()
    }
}
