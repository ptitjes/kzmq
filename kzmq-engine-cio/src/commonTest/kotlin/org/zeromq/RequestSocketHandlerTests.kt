/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

val RequestSocketHandlerTests by testSuite {
    val factory = ::RequestSocketHandler

    test("SHALL prefix the outgoing message with an empty delimiter frame") {
        factory.runTest {
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val request = Message { writeFrame("Hello") }

            send(request)

            assertReceivesExactly(
                listOf(Message {
                    writeEmptyFrame()
                    writeFrame("Hello")
                }),
                peer.sendChannel,
            )
        }
    }

    test("SHALL route outgoing messages to connected peers using a round-robin strategy", TestConfig.disable()) {
        factory.runTest {
            val peerCount = 5
            val messageCount = 10

            val peers = List(peerCount) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            repeat(messageCount) { messageIndex ->
                peers.forEachIndexed { peerIndex, peer ->
                    send(Message {
                        writeFrame("REQUEST".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    })

                    assertReceivesExactly(
                        listOf(Message {
                            writeEmptyFrame()
                            writeFrame("REQUEST".encodeToByteString())
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        }),
                        peer.sendChannel,
                    )

                    peer.receiveChannel.send(CommandOrMessage(Message {
                        writeEmptyFrame()
                        writeFrame("REPLY".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    }))

                    assertReceivesExactly(
                        listOf(Message {
                            writeFrame("REPLY".encodeToByteString())
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        }),
                        ::receive,
                    )
                }
            }
        }
    }

    suspendingSendTests(factory)

    test(
        "SHALL accept an incoming message only from the last peer that it sent a request to",
        TestConfig.testScope(isEnabled = false)
    ) {
        factory.runTest {
            val peers = List(2) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            send(Message { writeFrame("REQUEST".encodeToByteString()) })

            assertReceivesExactly(
                listOf(Message {
                    writeEmptyFrame()
                    writeFrame("REQUEST".encodeToByteString())
                }),
                peers[0].sendChannel,
            )

            peers[1].receiveChannel.send(CommandOrMessage(Message {
                writeEmptyFrame()
                writeFrame("IGNORED-REPLY".encodeToByteString())
            }))

            assertReceivesNothing(::receive)
        }
    }

    test("SHALL discard silently any messages received from other peers", TestConfig.testScope(isEnabled = false)) {
        factory.runTest {
            val peers = List(2) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            send(Message { writeFrame("REQUEST".encodeToByteString()) })

            assertReceivesExactly(
                listOf(Message {
                    writeEmptyFrame()
                    writeFrame("REQUEST".encodeToByteString())
                }),
                peers[0].sendChannel,
            )

            repeat(10) {
                peers[1].receiveChannel.send(CommandOrMessage(Message {
                    writeEmptyFrame()
                    writeFrame("IGNORED-REPLY".encodeToByteString())
                }))
            }

            assertReceivesNothing(::receive)

            peers[0].receiveChannel.send(CommandOrMessage(Message {
                writeEmptyFrame()
                writeFrame("REPLY".encodeToByteString())
            }))

            assertReceivesExactly(listOf(Message { writeFrame("REPLY".encodeToByteString()) }), ::receive)
        }
    }

    suspendingReceiveTests(
        factory = factory,
        configureForReceiver = {
            setState(RequestSocketState.AwaitingReply(it))
        },
        modifySentMessage = { message ->
            message.pushPrefixAddress(listOf("dummy-address".encodeToByteString()))
        },
    )
}
