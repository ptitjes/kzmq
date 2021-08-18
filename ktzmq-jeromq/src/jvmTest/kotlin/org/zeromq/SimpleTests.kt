package org.zeromq

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SimpleTests {
    @Test
    fun testSimple() = runBlocking {
        val context = Context(JeroMQ)
        val sent = "Hello 0MQ!".encodeToByteArray()

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testSimple")

            delay(100)
            publisher.send(Message(sent))
        }

        val subscription = launch {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testSimple")
            subscriber.subscribe("")

            val received = subscriber.receive().singleOrThrow()

            assertEquals(sent, received)
        }

        subscription.join()
        publication.join()
    }

    @Test
    fun testFlow() = runBlocking {
        val context = Context(JeroMQ)
        val testFlow = flow { for (i in 0..9) emit(i.toString().encodeToByteArray()) }

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testFlow")

            delay(100)
            testFlow.map { Message(it) }.collectToSocket(publisher)
        }

        val subscription = launch {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testFlow")
            subscriber.subscribe("")

            val values = subscriber.consumeAsFlow().take(10).map { it.singleOrThrow() }

            assertContentEquals(
                testFlow.map { it.decodeToString() }.toList(),
                values.map { it.decodeToString() }.toList()
            )
        }

        subscription.join()
        publication.join()
    }
}
