/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.fragments

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*
import kotlin.test.*

/**
 * Contributes tests for suspending sends.
 *
 * This tests covers:
 * - SHALL suspend on sending when it has no available peer
 * - SHALL not accept further messages when it has no available peers
 * - SHALL NOT discard messages that it cannot queue
 */
@TestDiscoverable
internal fun <H : SocketHandler> TestSuite.suspendingSendTests(
    factory: (SocketOptions) -> H,
    configureForSender: H.(PeerMailbox) -> Unit = {},
) =
    testSuite("SHALL suspend on sending", testConfig = TestConfig.testScope(isEnabled = false)) {
        test("no peer") {
            factory.runTest {
                // Trigger an asynchronous sending
                val result = async { send(Message { writeFrame("Won't be sent") }) }

                // The sending is suspending
                assertSuspends { result.await() }

                result.cancelAndJoin()
            }
        }

        test("no available peer") {
            factory.runTest {
                // A peer appears with a send queue size of 1
                val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                handler.configureForSender(peer)

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(Message { writeFrame("Queued") }))

                // Trigger an asynchronous sending
                val result = async { send(Message { writeFrame("Won't be sent") }) }

                // The sending is suspending
                assertSuspends { result.await() }

                result.cancelAndJoin()
            }
        }

        test("available peer appears during send") {
            factory.runTest {
                val peer = PeerMailbox("peer", SocketOptions())

                // Trigger an asynchronous sending
                val result = async { send(Message { writeFrame("Won't be sent immediately") }) }

                // The sending is suspending
                assertSuspends { result.await() }

                // An available peer appears
                peer.also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                handler.configureForSender(peer)

                // The sending eventually succeeds
                assertEquals(Unit, result.await())
            }
        }

        test("peer becomes available during send") {
            factory.runTest {
                // A peer appears with a send queue size of 1
                val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                handler.configureForSender(peer)

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(Message { writeFrame("Queued") }))

                // Trigger an asynchronous sending
                val result = async { send(Message { writeFrame("Won't be sent immediately") }) }

                // The sending is suspending
                assertSuspends { result.await() }

                // Clear the peer's send queue
                peer.sendChannel.receive()

                // The sending eventually succeeds
                assertEquals(Unit, result.await())
            }
        }

        test("other available peer appears during send") {
            factory.runTest {
                // A peer appears with a send queue size of 1
                val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                val anotherPeer = PeerMailbox("other peer", SocketOptions())

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(Message { writeFrame("Queued") }))

                // Trigger an asynchronous sending
                val result = async { send(Message { writeFrame("Won't be sent immediately") }) }

                // The sending is suspending
                assertSuspends { result.await() }

                // Another available peer appears
                anotherPeer.also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                handler.configureForSender(anotherPeer)

                // The sending eventually succeeds
                assertEquals(Unit, result.await())
            }
        }
    }
