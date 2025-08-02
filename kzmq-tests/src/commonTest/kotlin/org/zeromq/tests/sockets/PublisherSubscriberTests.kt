/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

@Suppress("unused")
val PublisherSubscriberTests by testSuite {

    withContexts("bind-connect") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val message = Message { writeFrame("Hello, 0MQ!".encodeToByteString()) }

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("")

        waitForSubscriptions()

        publisher.send(message.copy())
        assertReceivesExactly(listOf(message), subscriber)
    }

    // TODO Figure out why this test is hanging with JeroMQ and ZeroMQ.js
    withContexts("connect-bind").config(
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val message = Message { writeFrame("Hello, 0MQ!".encodeToByteString()) }

        val subscriber = ctx2.createSubscriber().apply { bind(address) }
        val publisher = ctx1.createPublisher().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("")

        waitForSubscriptions()

        publisher.send(message.copy())
        assertReceivesExactly(listOf(message), subscriber)
    }

    withContexts("subscription filter") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val sent = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expected = sent.filter { it.startsWith("prefix") }

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("prefix")

        waitForSubscriptions()

        coroutineScope {
            launch {
                sent.forEach { publisher.send(Message(it.encodeToByteString())) }
            }

            launch {
                val received = mutableListOf<String>()
                repeat(2) {
                    received += subscriber.receive().singleOrThrow().readByteArray().decodeToString()
                }
                assertEquals(expected, received)
            }
        }
    }
}
