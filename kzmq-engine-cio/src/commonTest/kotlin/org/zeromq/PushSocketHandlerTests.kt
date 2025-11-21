/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.core.*
import io.kotest.assertions.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*
import kotlin.time.Duration.Companion.seconds

val PushSocketHandlerTests by testSuite {
    suspend fun TestExecutionScope.withHandler(test: SocketHandlerTest) =
        withSocketHandler(PushSocketHandler(), test)

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        withHandler { peerEvents, send, _ ->
            val peer1 = PeerMailbox("1", SocketOptions())
            val peer2 = PeerMailbox("2", SocketOptions().apply { sendQueueSize = 5 })

            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer1))
            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer2))

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer1))

            val firstBatch = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }
            val secondBatch = messages(10) { index -> writeFrame { writeByte((index + 10).toByte()) } }

            // Send each message of the first batch once per peer
            firstBatch.forEach { message -> repeat(2) { send(message.buildMessage()) } }
            // Send each message of the second batch once
            secondBatch.forEach { message -> send(message.buildMessage()) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer2))

            all {
                peer1.sendChannel shouldReceiveExactly firstBatch + secondBatch
                peer2.sendChannel shouldReceiveExactly firstBatch
            }
        }
    }

    test("SHALL route outgoing messages to available peers using a round-robin strategy") {
        withHandler { peerEvents, send, _ ->
            val peers = List(5) { index -> PeerMailbox(index.toString(), SocketOptions()) }

            peers.forEach { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            // Send each message once per peer
            repeat(10) { messageIndex ->
                repeat(peers.size) { peerIndex ->
                    send(message {
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    }.buildMessage())
                }
            }

            all {
                peers.forEachIndexed { peerIndex, peer ->
                    peer.sendChannel shouldReceiveExactly
                        messages(10) { messageIndex ->
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        }
                }
            }
        }
    }

    test("SHALL suspend on sending when it has no available peers") {
        withHandler { _, send, _ ->
            val message = buildMessage { writeFrame("Won't be sent".encodeToByteString()) }

            withTimeoutOrNull(1.seconds) {
                send(message)
            } shouldBe null
        }
    }

    test("SHALL not accept further messages when it has no available peers") {
        withHandler { _, send, _ ->
            val message = buildMessage { writeFrame("Won't be sent".encodeToByteString()) }

            withTimeoutOrNull(1.seconds) {
                send(message)
            } shouldBe null
        }
    }

    test("SHALL NOT discard messages that it cannot queue") {
        withHandler { peerEvents, send, _ ->
            val peer = PeerMailbox("1", SocketOptions())
            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))

            val messages = messages(10) { index -> writeFrame { writeByte(index.toByte()) } }

            // Send each message once
            messages.forEach { send(it.buildMessage()) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            // Check each receiver got every messages
            peer.sendChannel shouldReceiveExactly messages
        }
    }
}
