/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.fragments

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import kotlinx.io.*
import org.zeromq.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*
import kotlin.test.*

/**
 * Contributes tests for suspending receives.
 *
 * This tests covers:
 * - SHALL suspend on receiving when it has no incoming message
 */
@TestDiscoverable
internal fun <H : SocketHandler> TestSuite.suspendingReceiveTests(
    factory: (SocketOptions) -> H,
    configureForReceiver: H.(PeerMailbox) -> Unit = {},
    modifySentMessage: (Message) -> Unit = {},
    modifyReceivedMessage: (Message) -> Unit = {},
) =
    testSuite("SHALL suspend on receiving", testConfig = TestConfig.testScope(isEnabled = false)) {
        test("no incoming message") {
            factory.runTest {
                // Trigger an asynchronous receive
                val result = async { receive() }

                // The receive is suspending
                assertSuspends { result.await() }

                result.cancelAndJoin()
            }
        }

        test("incoming message arrives during receive") {
            factory.runTest {
                // A peer appears
                val peer = PeerMailbox("peer", SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                handler.configureForReceiver(peer)

                // Trigger an asynchronous receive
                val result = async { receive() }

                // The receive is suspending
                assertSuspends { result.await() }

                // Fill the peer's receive queue
                val receivedMessage = Message { writeFrame("Received") }.also { modifySentMessage(it) }
                peer.receiveChannel.send(CommandOrMessage(receivedMessage))

                // The receive eventually succeeds
                val received = result.await()
                assertNotNull(received)
                modifyReceivedMessage(received)
                assertEquals("Received", received.singleOrThrow().readString())
            }
        }
    }
