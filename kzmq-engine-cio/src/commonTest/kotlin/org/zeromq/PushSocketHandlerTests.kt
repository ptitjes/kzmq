/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

internal class PushSocketHandlerTests : FunSpec({
    val factory = ::PushSocketHandler

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        factory.runTest { peerEvents, send, _ ->
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
        factory.runTest { peerEvents, send, _ ->
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

    suspendingSendTests(factory)
})
