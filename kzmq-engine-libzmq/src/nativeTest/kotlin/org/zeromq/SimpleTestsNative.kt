/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.core.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import kotlin.test.*

@OptIn(ExperimentalForeignApi::class)
val SimpleTestsNative by testSuite(testConfig = TestConfig.disable()) {
    test("simple1") {
        println("Hello native")
        val context = Context(Libzmq)
        val message = "Hello 0MQ!".encodeToByteString()

        val publication = launch(Dispatchers.IO) {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testSimple")

            println("Before delay")
            delay(100)
            println("Sending message")
            publisher.send(Message(message))
            println("Sent message")
        }

        val subscription = launch(Dispatchers.IO) {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testSimple")
            subscriber.subscribe("")

            println("Receiving message")
            while (true) {
                val receivedMessage = subscriber.receive()
                val received = receivedMessage.readFrame { readByteString() }
                println("Received message: ${received.toHexString()}")
            }
        }

        subscription.join()
        publication.join()
    }

    test("simple2") {
        val context = Context(Libzmq)
        val sent = "Hello 0MQ!"

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

            val received = subscriber.receive().readFrame { readString() }
            assertEquals(sent, received)
        }

        subscription.join()
        publication.join()
    }

    test("flow") {
        val context = Context(Libzmq)
        val intFlow: Flow<Int> = flow { for (i in 0..9) emit(i) }

        val publication = launch {
            val publisher = context.createPublisher()
            publisher.bind("inproc://testFlow")

            delay(100)
            intFlow
                .map { Message(it.toString()) }
                .collectToSocket(publisher)
        }

        val subscription = launch {
            val subscriber = context.createSubscriber()
            subscriber.connect("inproc://testFlow")
            subscriber.subscribe("")

            val values = subscriber.consumeAsFlow().take(10).map { it.readFrame { readString().toInt() } }
            assertContentEquals(intFlow.toList(), values.toList())
        }

        subscription.join()
        publication.join()
    }
}
