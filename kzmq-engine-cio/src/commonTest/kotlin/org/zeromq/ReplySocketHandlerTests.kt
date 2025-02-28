/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

internal class ReplySocketHandlerTests : FunSpec({
    val factory = ::ReplySocketHandler

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        factory.runTest {
            val peerCount = 5
            val messageCount = 10

            val peers = List(peerCount) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            peers.forEachIndexed { peerIndex, peer ->
                launch {
                    repeat(messageCount) { messageIndex ->
                        peer.receiveChannel.send(CommandOrMessage(message {
                            writeFrame("dummy-address-$messageIndex#1")
                            writeFrame("dummy-address-$messageIndex#2")
                            writeFrame("dummy-address-$messageIndex#3")
                            writeEmptyFrame()
                            writeFrame("REQUEST".encodeToByteString())
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        }.buildMessage()))
                    }
                }
            }

            all {
                repeat(messageCount) { messageIndex ->
                    peers.forEachIndexed { peerIndex, peer ->
                        ::receive shouldReceiveExactly listOf(message {
                            writeFrame("REQUEST".encodeToByteString())
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        })

                        send(message {
                            writeFrame("REPLY".encodeToByteString())
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        }.buildMessage())

                        peer.sendChannel shouldReceiveExactly listOf(message {
                            writeFrame("dummy-address-$messageIndex#1")
                            writeFrame("dummy-address-$messageIndex#2")
                            writeFrame("dummy-address-$messageIndex#3")
                            writeEmptyFrame()
                            writeFrame("REPLY".encodeToByteString())
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(peerIndex.toByte()) }
                        })
                    }
                }
            }
        }
    }

    suspendingSendTests(
        factory = factory,
        configureForSender = {
            setState(ReplySocketState.ProcessingRequest(it, listOf("dummy-address".encodeToByteString())))
        },
    )

    suspendingReceiveTests(
        factory = factory,
        modifySentMessage = { message ->
            message.pushPrefixAddress(listOf("dummy-address".encodeToByteString()))
        },
    )
})
