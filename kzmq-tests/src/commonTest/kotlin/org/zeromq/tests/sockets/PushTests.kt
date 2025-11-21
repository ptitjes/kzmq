/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.core.*
import io.kotest.assertions.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.test.*
import org.zeromq.tests.utils.*
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
val PushTests by testSuite {

    dualContextTest("simple connect-bind") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val pushSocket = ctx1.createPush().apply { connect(address) }
        val pullSocket = ctx2.createPull().apply { bind(address) }

        waitForConnections()

        val template = message {
            writeFrame("Hello, 0MQ!".encodeToByteString())
        }
        pushSocket.send(template)
        pullSocket shouldReceive template

        pushSocket.close()
        pullSocket.close()
    }

    dualContextTest("simple bind-connect") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val pushSocket = ctx1.createPush().apply { bind(address) }
        val pullSocket = ctx2.createPull().apply { connect(address) }

        waitForConnections()

        val template = message {
            writeFrame("Hello, 0MQ!".encodeToByteString())
        }
        pushSocket.send(template)
        pullSocket shouldReceive template

        pushSocket.close()
        pullSocket.close()
    }

    dualContextTest("lingers after disconnect", config = {
        // TODO investigate why this fails with these engines
        skip("cio-jeromq", "jeromq-cio")
    }) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val messageCount = 100

        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSocket = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        val templates = messages(messageCount) { index ->
            writeFrame(ByteString(index.toByte()))
        }

        templates.forEach { pushSocket.send(it) }
        pushSocket.disconnect(address)

        pullSocket shouldReceiveExactly templates

        pushSocket.close()
        pullSocket.close()
    }

    dualContextTest("lingers after close", config = {
        // TODO investigate why this fails with these engines
        skip("cio")
    }) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val messageCount = 100

        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSocket = ctx1.createPush().apply { connect(address) }

        waitForConnections()

        val templates = messages(messageCount) { index ->
            writeFrame(ByteString(index.toByte()))
        }

        templates.forEach { pushSocket.send(it) }
        pushSocket.close()

        pullSocket shouldReceiveExactly templates

        pullSocket.close()
    }

    dualContextTest("SHALL consider a peer as available only when it has an outgoing queue that is not full") { ctx1, ctx2, protocol ->
        val address1 = randomEndpoint(protocol)
        val address2 = randomEndpoint(protocol)

        val firstBatch = messages(5) { index ->
            writeFrame(ByteString(index.toByte()))
        }
        val secondBatch = messages(10) { index ->
            writeFrame(ByteString((index + 10).toByte()))
        }

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
            firstBatch.forEach { template -> repeat(2) { push.send(template) } }
            // Send each message of the second batch once
            secondBatch.forEach { template -> push.send(template) }

            pull2.apply { bind(address2) }
            waitForConnections()

            all {
                pull1 shouldReceiveExactly firstBatch + secondBatch
                pull2 shouldReceiveExactly firstBatch
            }
        }
    }

    dualContextTest("SHALL route outgoing messages to available peers using a round-robin strategy") { ctx1, ctx2, protocol ->
        val pullCount = 5
        val address = randomEndpoint(protocol)

        val push = ctx1.createPush().apply { bind(address) }
        val pulls = List(pullCount) { ctx2.createPull().apply { connect(address) } }

        waitForConnections(pullCount)

        val templates = messages(10) { index ->
            writeFrame(ByteString(index.toByte()))
        }

        // Send each message once per receiver
        templates.forEach { template -> repeat(pulls.size) { push.send(template) } }

        all {
            // Check each receiver got every messages
            pulls.forEach { it shouldReceiveExactly templates }
        }
    }

    singleContextTest("SHALL suspend on sending when it has no available peers", config = {
        // TODO investigate why this fails with these engines
        skip("jeromq", "zeromq.js")
    }) { ctx, _ ->
        val push = ctx.createPush()

        val message = Message("Won't be sent".encodeToByteString())

        withTimeoutOrNull(1.seconds) {
            push.send(message)
        } shouldBe null
    }

    // TODO How is it different from the previous test?
    singleContextTest("SHALL not accept further messages when it has no available peers", config = {
        // TODO investigate why this fails with these engines
        skip("jeromq", "zeromq.js")
    }) { ctx, _ ->
        val push = ctx.createPush()

        val message = Message("Won't be sent".encodeToByteString())

        withTimeoutOrNull(1.seconds) {
            push.send(message)
        } shouldBe null
    }

    dualContextTest("SHALL NOT discard messages that it cannot queue", config = {
        // TODO investigate why this fails with these engines
        skipAll()
    }) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val push = ctx1.createPush().apply { connect(address) }

        val templates = messages(10) { index -> listOf(ByteString(index.toByte())) }

        // Send each message once
        templates.forEach { push.send(it) }

        val pull = ctx2.createPull().apply { bind(address) }
        waitForConnections()

        // Check each receiver got every messages
        pull shouldReceiveExactly templates
    }
}
