/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.core.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*
import kotlin.time.Duration.Companion.seconds

val RequestSocketHandlerTests by testSuite {
    suspend fun TestExecutionScope.withHandler(test: SocketHandlerTest) =
        withSocketHandler(RequestSocketHandler(), test)

    test("SHALL prefix the outgoing message with an empty delimiter frame") {
        withHandler { peerEvents, send, _ ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val request = message { writeFrame("Hello") }

            send.send(request)

            peer.sendChannel shouldReceiveExactly listOf(message {
                writeEmptyFrame()
                writeFrame("Hello")
            })
        }
    }

    test("SHALL route outgoing messages to connected peers using a round-robin strategy") {
        withHandler { peerEvents, send, receive ->
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
                    send(message {
                        writeFrame("REQUEST".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    }.buildMessage())

                    peer.sendChannel shouldReceiveExactly listOf(message {
                        writeEmptyFrame()
                        writeFrame("REQUEST".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    })

                    peer.receiveChannel.send(CommandOrMessage(message {
                        writeEmptyFrame()
                        writeFrame("REPLY".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    }.buildMessage()))

                    receive shouldReceiveExactly listOf(message {
                        writeFrame("REPLY".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    })
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


    test("SHALL accept an incoming message only from the last peer that it sent a request to") {
        withHandler { peerEvents, send, receive ->
            val peers = List(2) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            send(message {
                writeFrame("REQUEST".encodeToByteString())
            }.buildMessage())

            peers[0].sendChannel shouldReceiveExactly listOf(message {
                writeEmptyFrame()
                writeFrame("REQUEST".encodeToByteString())
            })

            peers[1].receiveChannel.send(CommandOrMessage(message {
                writeEmptyFrame()
                writeFrame("IGNORED-REPLY".encodeToByteString())
            }.buildMessage()))

            receive.shouldReceiveNothing()
        }
    }

    test("SHALL discard silently any messages received from other peers") {
        withHandler { peerEvents, send, receive ->
            val peers = List(2) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            send(message {
                writeFrame("REQUEST".encodeToByteString())
            }.buildMessage())

            peers[0].sendChannel shouldReceiveExactly listOf(message {
                writeEmptyFrame()
                writeFrame("REQUEST".encodeToByteString())
            })

            repeat(10) {
                peers[1].receiveChannel.send(CommandOrMessage(message {
                    writeEmptyFrame()
                    writeFrame("IGNORED-REPLY".encodeToByteString())
                }.buildMessage()))
            }

            receive.shouldReceiveNothing()

            peers[0].receiveChannel.send(CommandOrMessage(message {
                writeEmptyFrame()
                writeFrame("REPLY".encodeToByteString())
            }.buildMessage()))

            receive shouldReceiveExactly listOf(message {
                writeFrame("REPLY".encodeToByteString())
            })
        }
    }
}
