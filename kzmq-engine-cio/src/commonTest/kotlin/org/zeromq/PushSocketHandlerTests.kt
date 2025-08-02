/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.utils.*

val PushSocketHandlerTests by testSuite {
    val factory = ::PushSocketHandler

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        factory.runTest {
            val peer1 = PeerMailbox("1", SocketOptions())
            val peer2 = PeerMailbox("2", SocketOptions().apply { sendQueueSize = 5 })

            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer1))
            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer2))

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer1))

            val firstBatch = List(5) { index -> Message(ByteString(index.toByte())) }
            val secondBatch = List(10) { index -> Message(ByteString((index + 10).toByte())) }

            // Send each message of the first batch once per peer
            firstBatch.forEach { message -> repeat(2) { send(message) } }
            // Send each message of the second batch once
            secondBatch.forEach { message -> send(message) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer2))

            assertReceivesExactly(firstBatch + secondBatch, peer1.sendChannel)
            assertReceivesExactly(firstBatch, peer2.sendChannel)
        }
    }

    test("SHALL route outgoing messages to available peers using a round-robin strategy") {
        factory.runTest {
            val peers = List(5) { index -> PeerMailbox(index.toString(), SocketOptions()) }

            peers.forEach { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            // Send each message once per peer
            repeat(10) { messageIndex ->
                repeat(peers.size) { peerIndex ->
                    send(Message {
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    })
                }
            }

            peers.forEachIndexed { peerIndex, peer ->
                assertReceivesExactly(
                    List(10) { messageIndex ->
                        Message {
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        }
                    },
                    peer.sendChannel,
                )
            }
        }
    }

    suspendingSendTests(factory)
}
