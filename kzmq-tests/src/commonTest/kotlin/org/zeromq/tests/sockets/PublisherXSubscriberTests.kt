/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
val PublisherXSubscriberTests by testSuite {

    withContexts("subscription filter") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val sent = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expected = sent.filter { it.startsWith("prefix") }

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createXSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.send(SubscriptionMessage(true, "prefix".encodeToByteString()).toMessage())

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
