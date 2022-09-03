/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class PublisherSubscriberTests : FunSpec({

    withContexts("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch { publisher.send(message) }
            launch { subscriber.receive() shouldBe message }
        }
    }

    // TODO Figure out why this test is hanging with JeroMQ and ZeroMQ.js
    withContexts("connect-bind").config(skip = setOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val subscriber = ctx2.createSubscriber().apply { bind(address) }
        val publisher = ctx1.createPublisher().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch { publisher.send(message) }
            launch { subscriber.receive() shouldBe message }
        }
    }

    withContexts("subscription filter") { (ctx1, ctx2) ->
        val address = randomAddress()

        val sent = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expected = sent.filter { it.startsWith("prefix") }

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("prefix")

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
