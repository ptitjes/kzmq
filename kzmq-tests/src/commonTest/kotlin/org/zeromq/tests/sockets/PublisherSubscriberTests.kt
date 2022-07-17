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

private val HELLO = constantFrameOf("Hello 0MQ!")

@Suppress("unused")
class PublisherSubscriberTests : FunSpec({

    withEngines("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()

        val publisher = ctx1.createPublisher()
        publisher.bind(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.connect(address)
        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch { publisher.send(messageOf(HELLO)) }
            launch { subscriber.receive() shouldBe messageOf(HELLO) }
        }
    }

    // TODO Figure out why this test is hanging with JeroMQ and ZeroMQ.js
    withEngines("connect-bind").config(skipEngines = listOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
        val address = randomAddress()

        val publisher = ctx1.createPublisher()
        publisher.connect(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.bind(address)
        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch { publisher.send(messageOf(HELLO)) }
            launch { subscriber.receive() shouldBe messageOf(HELLO) }
        }
    }

    withEngines("flow") { (ctx1, ctx2) ->
        val address = randomAddress()
        val count = 10
        val frames = generateRandomFrames(count)

        val publisher = ctx1.createPublisher()
        publisher.bind(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.connect(address)
        subscriber.subscribe("")

        waitForSubscriptions()

        coroutineScope {
            launch {
                frames.asFlow().map { messageOf(it) }.collectToSocket(publisher)
            }

            launch {
                val received = subscriber.consumeAsFlow().take(count)
                received.toList().map { it.removeFirst() } shouldContainExactly frames
            }
        }
    }

    withEngines("select").config(skipEngines = listOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
        val address1 = randomAddress()
        val address2 = randomAddress()

        val count = 10
        val frames = generateRandomFrames(count).sortedWith(FrameComparator)

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
                for ((index, frame) in frames.withIndex()) {
                    (if (index % 2 == 0) publisher1 else publisher2).send(messageOf(frame))
                }
            }

            launch {
                val received = mutableListOf<Frame>()
                repeat(count) {
                    received += select<Message> {
                        subscriber1.onReceive { it }
                        subscriber2.onReceive { it }
                    }.removeFirst()
                }
                received.sortedWith(FrameComparator) shouldContainExactly frames
            }
        }
    }

    withEngines("subscription filter") { (ctx1, ctx2) ->
        val address = randomAddress()

        val data = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expectedData = data.filter { it.startsWith("prefix") }

        val frames = data.map { constantFrameOf(it) }
        val expectedFrames = expectedData.map { constantFrameOf(it) }

        val publisher = ctx1.createPublisher()
        publisher.bind(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.connect(address)
        subscriber.subscribe("prefix")

        waitForSubscriptions()

        coroutineScope {
            launch {
                frames.forEach { publisher.send(messageOf(it)) }
            }

            launch {
                val received = mutableListOf<Frame>()
                repeat(2) {
                    received += subscriber.receive().removeFirst()
                }
                received shouldContainExactly expectedFrames
            }
        }
    }
})

private suspend fun waitForSubscriptions() = delay(200)
