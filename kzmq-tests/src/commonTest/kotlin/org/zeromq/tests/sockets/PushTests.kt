/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.assertions.*
import io.kotest.common.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKotest::class)
@Suppress("unused")
class PushTests : FunSpec({

    withContexts("simple connect-bind") { ctx1, ctx2, protocol ->
        val address = randomAddress(protocol)

        val pushSocket = ctx1.createPush().apply { connect(address) }
        val pullSocket = ctx2.createPull().apply { bind(address) }

        waitForConnections()

        val message = Message("Hello, 0MQ!".encodeToByteArray())
        pushSocket.send(message)
        pullSocket shouldReceiveExactly listOf(message)

        pushSocket.close()
        pullSocket.close()
    }

    withContexts("simple bind-connect") { ctx1, ctx2, protocol ->
        val address = randomAddress(protocol)

        val pushSocket = ctx1.createPush().apply { bind(address) }
        val pullSocket = ctx2.createPull().apply { connect(address) }

        waitForConnections()

        val message = Message("Hello, 0MQ!".encodeToByteArray())
        pushSocket.send(message)
        pullSocket shouldReceiveExactly listOf(message)

        pushSocket.close()
        pullSocket.close()
    }

    withContexts("lingers after disconnect").config(
        // TODO investigate why these pairs are flaky
        skip = setOf("cio-jeromq", "jeromq-cio"),
    ) { ctx1, ctx2, protocol ->
        val address = randomAddress(protocol)
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
        skip = setOf("cio-jeromq", "jeromq-cio"),
    ) { ctx1, ctx2, protocol ->
        val address = randomAddress(protocol)
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

    withContexts("SHALL consider a peer as available only when it has an outgoing queue that is not full") { ctx1, ctx2, protocol ->
        val address1 = randomAddress(protocol)
        val address2 = randomAddress(protocol)

        val firstBatch = List(5) { index -> Message(ByteArray(1) { index.toByte() }) }
        val secondBatch = List(10) { index -> Message(ByteArray(1) { (index + 10).toByte() }) }

        val push = ctx1.createPush()
        val pull1 = ctx2.createPull()
        val pull2 = ctx2.createPull()

        listOf(push, pull1, pull2).use {
            push.apply {
                connect(address1)
                sendHighWaterMark = 5
                connect(address2)
            }

            pull1.apply { bind(address1) }
            waitForConnections()

            // Send each message of the first batch once per receiver
            firstBatch.forEach { message -> repeat(2) { push.send(message) } }
            // Send each message of the second batch once
            secondBatch.forEach { message -> push.send(message) }

            pull2.apply { bind(address2) }
            waitForConnections()

            all {
                pull1 shouldReceiveExactly firstBatch + secondBatch
                pull2 shouldReceiveExactly firstBatch
            }
        }
    }

    withContexts("SHALL route outgoing messages to available peers using a round-robin strategy") { ctx1, ctx2, protocol ->
        val pullCount = 5
        val address = randomAddress(protocol)

        val push = ctx1.createPush().apply { bind(address) }
        val pulls = List(pullCount) { ctx2.createPull().apply { connect(address) } }

        waitForConnections(pullCount)

        val messages = List(10) { index -> Message(ByteArray(1) { index.toByte() }) }

        // Send each message once per receiver
        messages.forEach { message -> repeat(pulls.size) { push.send(message) } }

        all {
            // Check each receiver got every messages
            pulls.forEach { it shouldReceiveExactly messages }
        }
    }

    withContext("SHALL suspend on sending when it has no available peers").config(
        // TODO investigate why this fails with these engines
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx, _ ->
        val push = ctx.createPush()

        val message = Message("Won't be sent".encodeToByteArray())

        withTimeoutOrNull(1.seconds) {
            push.send(message)
        } shouldBe null
    }

    // TODO How is it different from previous test?
    withContext("SHALL not accept further messages when it has no available peers").config(
        // TODO investigate why this fails with these engines
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx, _ ->
        val push = ctx.createPush()

        val message = Message("Won't be sent".encodeToByteArray())

        withTimeoutOrNull(1.seconds) {
            push.send(message)
        } shouldBe null
    }

    withContexts("SHALL NOT discard messages that it cannot queue") { ctx1, ctx2, protocol ->
        val address = randomAddress(protocol)

        val push = ctx1.createPush().apply { connect(address) }

        val messages = List(10) { index -> Message(ByteArray(1) { index.toByte() }) }

        // Send each message once
        messages.forEach { message -> push.send(message) }

        val pull = ctx2.createPull().apply { bind(address) }
        waitForConnections()

        // Check each receiver got every messages
        pull shouldReceiveExactly messages
    }
})
