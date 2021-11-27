/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
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
import kotlin.test.*

@Ignore
class PubSubTests : FunSpec({

    withEngines("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val publisher = ctx1.createPublisher()
        publisher.bind(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.connect(address)
        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch { publisher.send(message) }
            launch { subscriber.receive() shouldBe message }
        }
    }

    // TODO Figure out why this test is failing with JeroMQ
    withEngines("connect-bind").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val publisher = ctx1.createPublisher()
        publisher.connect(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.bind(address)
        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch { publisher.send(message) }
            launch { subscriber.receive() shouldBe message }
        }
    }

    withEngines("flow") { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 10
        val sent = generateMessages(messageCount).asFlow()

        val publisher = ctx1.createPublisher()
        publisher.bind(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.connect(address)
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

    withEngines("select").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address1 = randomAddress()
        val address2 = randomAddress()

        val messageCount = 10
        val sent = generateMessages(messageCount)

        val publisher1 = ctx1.createPublisher()
        publisher1.bind(address1)

        val publisher2 = ctx1.createPublisher()
        publisher2.bind(address2)

        val subscriber1 = ctx2.createSubscriber()
        subscriber1.connect(address1)
        subscriber1.subscribe("")

        val subscriber2 = ctx2.createSubscriber()
        subscriber2.connect(address2)
        subscriber2.subscribe("")

        waitForSubscriptions()

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
})

private suspend fun waitForSubscriptions() = delay(200)
