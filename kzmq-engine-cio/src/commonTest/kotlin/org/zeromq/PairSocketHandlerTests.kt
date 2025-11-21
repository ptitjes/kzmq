/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.core.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*
import kotlin.time.Duration.Companion.seconds

val PairSocketHandlerTests by testSuite {
    suspend fun TestExecutionScope.withHandler(test: SocketHandlerTest) =
        withSocketHandler(PairSocketHandler(), test)

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        withHandler { peerEvents, send, _ ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
            }

            val messages = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }

            // Send each message of the first batch once
            messages.forEach { send(it.buildMessage()) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            peer.sendChannel shouldReceiveExactly messages
        }
    }

    test("SHALL suspend on sending when it has no available peer") {
        withHandler { peerEvents, send, _ ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
            }

            val messages = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }
            val blockedMessage = message { writeFrame { writeByte((10).toByte()) } }

            // Send each message of the first batch once
            messages.forEach { send(it.buildMessage()) }

            withTimeoutOrNull(1.seconds) {
                // Send an additional message
                send(blockedMessage.buildMessage())
            } shouldBe null

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            peer.sendChannel shouldReceiveExactly messages
        }
    }

    test("SHALL not accept further messages when it has no available peer") {
        withHandler { peerEvents, send, _ ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
            }

            val messages = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }
            val blockedMessage = message { writeFrame { writeByte((10).toByte()) } }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            // Send each message of the first batch once
            messages.forEach { send(it.buildMessage()) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.DISCONNECTION, peer))

            withTimeoutOrNull(1.seconds) {
                // Send an additional message
                send(blockedMessage.buildMessage())
            } shouldBe null

            peer.sendChannel shouldReceiveExactly messages
        }
    }

    test("SHALL receive incoming messages from its single peer if it has one") {
        withHandler { peerEvents, _, receive ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }

            // Send each message of the first batch once
            messages.forEach { peer.receiveChannel.send(CommandOrMessage(it.buildMessage())) }

            receive shouldReceiveExactly messages
        }
    }
}
