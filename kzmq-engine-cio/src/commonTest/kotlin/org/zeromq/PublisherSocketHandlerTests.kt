/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

val PublisherSocketHandlerTests by testSuite {
    val factory = ::PublisherSocketHandler

    test("SHALL not modify outgoing messages in any way") {
        factory.runTest {
            val peer = PeerMailbox("1", SocketOptions()).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val message = Message { writeFrame("MESSAGE") }

            // Make the peer subscribe
            peer.receiveChannel.send(CommandOrMessage(SubscribeCommand("".encodeToByteString())))

            yield()

            send(message)

            assertHasReceivedExactly(listOf(message), peer.sendChannel)
        }
    }

    test("MAY, depending on the transport, send all messages to all subscribers") {
        factory.runTest {
            val peer = PeerMailbox("1", SocketOptions(), usesBroadcast = true).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val message = Message { writeFrame(ByteString(0x00)) }

            send(message)

            assertHasReceivedExactly(listOf(message), peer.sendChannel)
        }
    }

    testSuite("MAY, depending on the transport, send messages only to subscribers who have a matching subscription") {
        test("without subscription") {
            factory.runTest {
                val peer = PeerMailbox("1", SocketOptions(), usesBroadcast = false).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                val message = Message { writeFrame(ByteString(0x00)) }

                send(message)

                assertHasReceivedExactly(listOf(), peer.sendChannel)
            }
        }

        test("with subscription") {
            factory.runTest {
                val peer = PeerMailbox("1", SocketOptions(), usesBroadcast = false).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                val message = Message { writeFrame(ByteString(0x00)) }

                // Make the peer subscribe
                peer.receiveChannel.send(CommandOrMessage(SubscribeCommand(ByteString())))

                yield()

                send(message)

                assertHasReceivedExactly(listOf(message), peer.sendChannel)
            }
        }
    }

    testSuite("SHALL perform a binary comparison of the subscription against the start of the first frame of the message") {
        test("full subscription") {
            factory.runTest {
                val peer = PeerMailbox("1", SocketOptions(), usesBroadcast = false).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                val message = Message { writeFrame(ByteString(0x00)) }

                // Make the peer subscribe
                peer.receiveChannel.send(CommandOrMessage(SubscribeCommand(ByteString())))

                yield()

                send(message)

                assertHasReceivedExactly(listOf(message), peer.sendChannel)
            }
        }

        test("matching subscription") {
            factory.runTest {
                val peer = PeerMailbox("1", SocketOptions(), usesBroadcast = false).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                val message = Message { writeFrame(ByteString(0x00, 0x01)) }

                // Make the peer subscribe
                peer.receiveChannel.send(CommandOrMessage(SubscribeCommand(ByteString(0x00))))

                yield()

                send(message)

                assertHasReceivedExactly(listOf(message), peer.sendChannel)
            }
        }

        test("non-matching subscription") {
            factory.runTest {
                val peer = PeerMailbox("1", SocketOptions(), usesBroadcast = false).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                val message = Message { writeFrame(ByteString(0x00, 0x01)) }

                // Make the peer subscribe
                peer.receiveChannel.send(CommandOrMessage(SubscribeCommand(ByteString(0x01))))

                yield()

                send(message)

                assertHasReceivedExactly(listOf(), peer.sendChannel)
            }
        }
    }

    test("SHALL silently drop the message if the queue for a subscriber is full") {
        factory.runTest {
            val peer = PeerMailbox("1", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val queuedMessage = Message { writeFrame(ByteString(0x00)) }
            val droppedMessage = Message { writeFrame(ByteString(0x01)) }

            // Fill the peer's send queue
            peer.sendChannel.send(CommandOrMessage(queuedMessage))

            // Make the peer subscribe
            peer.receiveChannel.send(CommandOrMessage(SubscribeCommand("".encodeToByteString())))

            yield()

            send(droppedMessage)

            assertHasReceivedExactly(listOf(queuedMessage), peer.sendChannel)
        }
    }

    testSuite("SHALL NOT block on sending") {
        test("no peers") {
            factory.runTest {
                val message = Message { writeFrame("MESSAGE") }

                assertDoesNotSuspend { send(message) }
            }
        }

        test("no available peers") {
            factory.runTest {
                val peer = PeerMailbox("1", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(Message { writeFrame("Queued") }))

                val message = Message { writeFrame("MESSAGE") }

                // Make the peer subscribe
                peer.receiveChannel.send(CommandOrMessage(SubscribeCommand("".encodeToByteString())))

                yield()

                assertDoesNotSuspend { send(message) }
            }
        }
    }
}
