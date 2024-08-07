/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.fragments

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.internal.*
import org.zeromq.utils.*

/**
 * Contributes tests for suspending sends.
 *
 * This tests covers:
 * - SHALL suspend on sending when it has no available peer
 * - SHALL not accept further messages when it has no available peers
 * - SHALL NOT discard messages that it cannot queue
 */
internal fun FunSpec.suspendingSendTests(factory: () -> SocketHandler) =
    testSet("SHALL suspend on sending") {
        test("no peer") {
            factory.runTest { _, send, _ ->
                // Trigger an asynchronous sending
                val result = async { send(buildMessage { writeFrame("Won't be sent") }) }

                // The sending is suspending
                shouldSuspend { result.await() }

                result.cancelAndJoin()
            }
        }

        test("no available peer") {
            factory.runTest { peerEvents, send, _ ->
                // A peer appears with a send queue size of 1
                val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(buildMessage { writeFrame("Queued") }))

                // Trigger an asynchronous sending
                val result = async { send(buildMessage { writeFrame("Won't be sent") }) }

                // The sending is suspending
                shouldSuspend { result.await() }

                result.cancelAndJoin()
            }
        }

        test("available peer appears during send") {
            factory.runTest { peerEvents, send, _ ->
                // Trigger an asynchronous sending
                val result = async { send(buildMessage { writeFrame("Won't be sent immediately") }) }

                // The sending is suspending
                shouldSuspend { result.await() }

                // An available peer appears
                PeerMailbox("peer", SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                // The sending eventually succeeds
                result.await() shouldBe Unit
            }
        }

        test("peer becomes available during send") {
            factory.runTest { peerEvents, send, _ ->
                // A peer appears with a send queue size of 1
                val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(buildMessage { writeFrame("Queued") }))

                // Trigger an asynchronous sending
                val result = async { send(buildMessage { writeFrame("Won't be sent immediately") }) }

                // The sending is suspending
                shouldSuspend { result.await() }

                // Clear the peer's send queue
                peer.sendChannel.receive()

                // The sending eventually succeeds
                result.await() shouldBe Unit
            }
        }

        test("other available peer appears during send") {
            factory.runTest { peerEvents, send, _ ->
                // A peer appears with a send queue size of 1
                val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(buildMessage { writeFrame("Queued") }))

                // Trigger an asynchronous sending
                val result = async { send(buildMessage { writeFrame("Won't be sent immediately") }) }

                // The sending is suspending
                shouldSuspend { result.await() }

                // Another available peer appears
                PeerMailbox("other peer", SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                // The sending eventually succeeds
                result.await() shouldBe Unit
            }
        }
    }
