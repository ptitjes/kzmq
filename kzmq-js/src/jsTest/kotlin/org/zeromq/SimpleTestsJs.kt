/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class SimpleTestsJs {

    @Test
    fun testSimple() = runTest {
        val context = Context(JS)
        val sent = "Hello 0MQ!".encodeToByteArray()

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testSimple")

            withContext(Dispatchers.Default.limitedParallelism(1)) {
                delay(100)
            }

            println("Before send")
            publisher.send(Message(sent))
            println("Sent")
        }

        val subscription = launch {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testSimple")
            subscriber.subscribe("")

            println("Before receive")
            val received = subscriber.receive().singleOrThrow()
            println("Received")

            assertEquals(sent.decodeToString(), received.decodeToString())
        }

        subscription.join()
        publication.join()
    }

    @Test
    fun testFlow() = runTest {
        val context = Context(JS)
        val testFlow = flow { for (i in 0..9) emit(i.toString().encodeToByteArray()) }

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testFlow")

            delay(1000)
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
