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
class PushPullTests : FunSpec({

    withContexts("lingers after disconnect").config(
        // TODO investigate why these pairs are flaky
        skipEnginePairs = listOf("cio" to "jeromq", "jeromq" to "cio"),
    ) { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 100

        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSocket = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        var sent = 0
        while (sent < messageCount) {
            val message = Message(sent.encodeToByteArray())
            pushSocket.send(message)
            sent++
        }
        pushSocket.disconnect(address)

        var received = 0
        while (received < messageCount) {
            val message = pullSocket.receive()
            message.singleOrThrow().decodeToInt() shouldBe received
            received++
        }
        received shouldBe messageCount

        pushSocket.close()
        pullSocket.close()
    }

    withContexts("lingers after close").config(
        // TODO investigate why these pairs are flaky
        skipEnginePairs = listOf("cio" to "jeromq", "jeromq" to "cio"),
    ) { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 100

        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSocket = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        var sent = 0
        while (sent < messageCount) {
            val message = Message(sent.encodeToByteArray())
            pushSocket.send(message)
            sent++
        }
        pushSocket.close()

        var received = 0
        while (received < messageCount) {
            val message = pullSocket.receive()
            message.singleOrThrow().decodeToInt() shouldBe received
            received++
        }
        received shouldBe messageCount

        pullSocket.close()
    }

    withContexts("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush().apply { bind(address) }
        val pull = ctx2.createPull().apply { connect(address) }

        waitForConnections()

        push.send(message)
        pull.receive() shouldBe message
    }

    withContexts("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val pull = ctx2.createPull().apply { bind(address) }
        val push = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        push.send(message)
        pull.receive() shouldBe message
    }

    withContexts("flow") { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 10
        val sent = generateMessages(messageCount).asFlow()

        val push = ctx1.createPush().apply { bind(address) }
        val pull = ctx2.createPull().apply { connect(address) }

        waitForConnections()

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

        val push1 = ctx1.createPush().apply { bind(address1) }
        val push2 = ctx1.createPush().apply { bind(address2) }
        val pull1 = ctx2.createPull().apply { connect(address1) }
        val pull2 = ctx2.createPull().apply { connect(address2) }

        waitForConnections(2)

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
        val pullCount = 5
        val address = randomAddress(Protocol.TCP)

        val push = ctx1.createPush().apply { bind(address) }
        val pulls = List(pullCount) { ctx2.createPull().apply { connect(address) } }

        waitForConnections(pullCount)

        testRoundRobinDispatch(
            { push.send(it) },
            pulls.map { pull -> { pull.receive() } },
            10,
        )
    }
})
