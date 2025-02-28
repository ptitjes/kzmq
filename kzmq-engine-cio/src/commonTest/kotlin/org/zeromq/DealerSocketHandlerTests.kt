/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.all
import io.kotest.core.spec.style.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

internal class DealerSocketHandlerTests : FunSpec({
    val factory = ::DealerSocketHandler

    test("SHALL route outgoing messages to connected peers using a round-robin strategy") {
        factory.runTest { peerEvents, send, receive ->
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
                        writeFrame("REQUEST".encodeToByteString())
                        writeFrame { writeByte(messageIndex.toByte()) }
                        writeFrame { writeByte(peerIndex.toByte()) }
                    })

                    peer.receiveChannel.send(CommandOrMessage(message {
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

    suspendingSendTests(factory)

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        factory.runTest { peerEvents, _, receive ->
            val peers = List(5) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            val messages = messages(10) { index ->
                writeFrame { writeByte(index.toByte()) }
            }

            peers.forEach { peer ->
                messages.forEach { peer.receiveChannel.send(it) }
            }

            all {
                messages.forEach { message ->
                    receive shouldReceiveExactly List(peers.size) { message }
                }
            }
        }
    }

    suspendingReceiveTests(factory)
})
