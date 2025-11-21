/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.core.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
val XPublisherSubscriberTests by testSuite {

    dualContextTest("subscription filter") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val sent = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expected = sent.filter { it.startsWith("prefix") }

        val publisher = ctx1.createXPublisher().apply { bind(address) }
        val subscriber = ctx2.createSubscriber().apply { connect(address) }

        waitForConnections()

        coroutineScope {
            launch {
                subscriber.subscribe("prefix")
            }

            launch {
                val message = publisher.receive()
                val subscriptionMessage = message.toSubscriptionMessage()
                subscriptionMessage shouldNotBe null
                subscriptionMessage?.let { (subscribe, topic) ->
                    subscribe shouldBe true
                    topic.decodeToString() shouldBe "prefix"
                }
            }
        }

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
                received shouldContainExactly expected
            }
        }
    }
}
