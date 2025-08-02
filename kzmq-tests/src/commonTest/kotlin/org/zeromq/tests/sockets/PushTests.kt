/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.test.*
import org.zeromq.tests.utils.*

@Suppress("unused")
val PushTests by testSuite {

    withContexts("simple connect-bind") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val pushSocket = ctx1.createPush().apply { connect(address) }
        val pullSocket = ctx2.createPull().apply { bind(address) }

        waitForConnections()

        val message = Message { writeFrame("Hello, 0MQ!".encodeToByteString()) }
        pushSocket.send(message.copy())
        assertReceivesExactly(listOf(message), pullSocket)

        pushSocket.close()
        pullSocket.close()
    }

    withContexts("simple bind-connect") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val pushSocket = ctx1.createPush().apply { bind(address) }
        val pullSocket = ctx2.createPull().apply { connect(address) }

        waitForConnections()

        val message = Message { writeFrame("Hello, 0MQ!".encodeToByteString()) }
        pushSocket.send(message.copy())
        assertReceivesExactly(listOf(message), pullSocket)

        pushSocket.close()
        pullSocket.close()
    }

    withContexts("lingers after disconnect").config(
        // TODO investigate why these pairs are flaky
        skip = setOf("cio-jeromq", "jeromq-cio"),
    ) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val messageCount = 100

        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSocket = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        val messages = List(messageCount) { index -> Message(ByteString(index.toByte())) }

        messages.forEach { pushSocket.send(it.copy()) }
        pushSocket.disconnect(address)

        assertReceivesExactly(messages, pullSocket)

        pushSocket.close()
        pullSocket.close()
    }

    withContexts("lingers after close").config(
        // TODO investigate why these tests are flaky
        skip = setOf("cio"),
    ) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val messageCount = 100

        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSocket = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        val messages = List(messageCount) { index -> Message(ByteString(index.toByte())) }

        messages.forEach { pushSocket.send(it.copy()) }
        pushSocket.close()

        assertReceivesExactly(messages, pullSocket)

        pullSocket.close()
    }

    withContexts("SHALL consider a peer as available only when it has an outgoing queue that is not full") { ctx1, ctx2, protocol ->
        val address1 = randomEndpoint(protocol)
        val address2 = randomEndpoint(protocol)

        val firstBatch = List(5) { index -> Message(ByteString(index.toByte())) }
        val secondBatch = List(10) { index -> Message(ByteString((index + 10).toByte())) }

        val push = ctx1.createPush()
        val pull1 = ctx2.createPull()
        val pull2 = ctx2.createPull()

        use(push, pull1, pull2) {
            push.apply {
                connect(address1)
                sendHighWaterMark = 5
                connect(address2)
            }

            pull1.apply { bind(address1) }
            waitForConnections()

            // Send each message of the first batch once per receiver
            firstBatch.forEach { message -> repeat(2) { push.send(message.copy()) } }
            // Send each message of the second batch once
            secondBatch.forEach { message -> push.send(message.copy()) }

            pull2.apply { bind(address2) }
            waitForConnections()

            assertReceivesExactly(firstBatch + secondBatch, pull1)
            assertReceivesExactly(firstBatch, pull2)
        }
    }

    withContexts("SHALL route outgoing messages to available peers using a round-robin strategy") { ctx1, ctx2, protocol ->
        val pullCount = 5
        val address = randomEndpoint(protocol)

        val push = ctx1.createPush().apply { bind(address) }
        val pulls = List(pullCount) { ctx2.createPull().apply { connect(address) } }

        waitForConnections(pullCount)

        val messages = List(10) { index -> Message(ByteString(index.toByte())) }

        // Send each message once per receiver
        messages.forEach { message -> repeat(pulls.size) { push.send(message.copy()) } }

        // Check each receiver got every messages
        pulls.forEach { assertReceivesExactly(messages, it) }
    }

    withContext("SHALL suspend on sending when it has no available peers").config(
        // TODO investigate why this fails with these engines
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx, _ ->
        val push = ctx.createPush()

        val message = Message("Won't be sent".encodeToByteString())

        assertSuspends { push.send(message) }
    }

    // TODO How is it different from previous test?
    withContext("SHALL not accept further messages when it has no available peers").config(
        // TODO investigate why this fails with these engines
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx, _ ->
        val push = ctx.createPush()

        val message = Message("Won't be sent".encodeToByteString())

        assertSuspends { push.send(message) }
    }

    withContexts("SHALL NOT discard messages that it cannot queue").config(
        only = setOf(),
    ) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val push = ctx1.createPush().apply { connect(address) }

        val messages = List(10) { index -> Message(ByteString(index.toByte())) }

        // Send each message once
        messages.forEach { push.send(it.copy()) }

        val pull = ctx2.createPull().apply { bind(address) }
        waitForConnections()

        // Check each receiver got every messages
        assertReceivesExactly(messages, pull)
    }
}
