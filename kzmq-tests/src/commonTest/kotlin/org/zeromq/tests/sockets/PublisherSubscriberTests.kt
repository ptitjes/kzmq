/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.*
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
    withContexts("connect-bind").config(skipEngines = listOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
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

    withContexts("flow") { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 10
        val sent = generateMessages(messageCount).asFlow()

        val publisher = ctx1.createPublisher().apply { bind(address) }
        val subscriber = ctx2.createSubscriber().apply { connect(address) }

        waitForConnections()

        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch {
                sent.collectToSocket(publisher)
            }

            launch {
                val received = subscriber.consumeAsFlow().take(messageCount)
                received.toList() shouldContainExactly sent.toList()
            }
        }
    }

    withContexts("select").config(skipEngines = listOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
        val address1 = randomAddress()
        val address2 = randomAddress()

        val messageCount = 10
        val sent = generateMessages(messageCount)

        val publisher1 = ctx1.createPublisher().apply { bind(address1) }
        val publisher2 = ctx1.createPublisher().apply { bind(address2) }
        val subscriber1 = ctx2.createSubscriber().apply { connect(address1) }
        val subscriber2 = ctx2.createSubscriber().apply { connect(address2) }

        waitForConnections(2)

        subscriber1.subscribe("")
        subscriber2.subscribe("")

        waitForSubscriptions(2)

        coroutineScope {
            launch {
                for ((index, message) in sent.withIndex()) {
                    (if (index % 2 == 0) publisher1 else publisher2).send(message)
                }
            }

            launch {
                val received = mutableListOf<Message>()
                repeat(messageCount) {
                    received += select<Message> {
                        subscriber1.onReceive { it }
                        subscriber2.onReceive { it }
                    }
                }
                received.sortedWith(MessageComparator) shouldContainExactly sent
            }
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
