/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class PublisherXSubscriberTests : FunSpec({

    withContexts("subscription filter") { ctx1, ctx2, protocol ->
        val address = randomAddress(protocol)

        val sent = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expected = sent.filter { it.startsWith("prefix") }

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createXSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.send(subscriptionMessageOf(true, "prefix".encodeToByteArray()))

        waitForSubscriptions()

        coroutineScope {
            launch {
                sent.forEach { publisher.send(Message(it.encodeToByteArray())) }
            }

            launch {
                val received = mutableListOf<String>()
                repeat(2) {
                    received += subscriber.receive().singleOrThrow().decodeToString()
                }
                received shouldContainExactly expected
            }
        }
    }
})
