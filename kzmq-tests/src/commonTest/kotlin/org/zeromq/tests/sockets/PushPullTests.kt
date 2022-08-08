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
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
class PushPullTests : FunSpec({

    withContexts("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        push.send(message)
        pull.receive() shouldBe message
    }

    withContexts("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.connect(address)

        val pull = ctx2.createPull()
        pull.bind(address)

        push.send(message)
        pull.receive() shouldBe message
    }

    withContexts("flow") { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 10
        val sent = generateMessages(messageCount).asFlow()

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        coroutineScope {
            launch {
                sent.collectToSocket(push)
            }

            launch {
                val received = pull.consumeAsFlow().take(messageCount)
                received.toList() shouldContainExactly sent.toList()
            }
        }
    }

    withContexts("select").config(skipEngines = listOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
        val address1 = randomAddress()
        val address2 = randomAddress()

        val messageCount = 10
        val sent = generateMessages(messageCount)

        val push1 = ctx1.createPush()
        push1.bind(address1)

        val push2 = ctx1.createPush()
        push2.bind(address2)

        val pull1 = ctx2.createPull()
        pull1.connect(address1)

        val pull2 = ctx2.createPull()
        pull2.connect(address2)

        coroutineScope {
            launch {
                for ((index, message) in sent.withIndex()) {
                    (if (index % 2 == 0) push1 else push2).send(message)
                }
            }

            launch {
                val received = mutableListOf<Message>()
                repeat(messageCount) {
                    received += select<Message> {
                        pull1.onReceive { it }
                        pull2.onReceive { it }
                    }
                }
                received.sortedWith(MessageComparator) shouldContainExactly sent
            }
        }
    }

    // TODO we are not handling peer availability (i.e. send queue is full)
    withContexts("Push SHALL route outgoing messages to available peers using a round-robin strategy") { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.TCP)

        val pushSocket = ctx1.createPush().apply { bind(address) }
        val pullSockets = List(5) { ctx2.createPull().apply { connect(address) } }

        delay(500.milliseconds)

        testRoundRobin(
            { pushSocket.send(it) },
            pullSockets.map { pullSocket -> { pullSocket.receive() } },
            10,
        )
    }

    withContexts("Pull SHALL receive incoming messages from its peers using a fair-queuing strategy") { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.TCP)

        val pushSockets = List(5) { ctx2.createPush().apply { connect(address) } }
        val pullSocket = ctx1.createPull().apply { bind(address) }

        delay(1000.milliseconds)

        testFairQueuing(
            pushSockets.map { pushSocket -> { pushSocket.send(it) } },
            { pullSocket.receive() },
            10,
        )
    }
})
