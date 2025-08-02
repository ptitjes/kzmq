/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*
import kotlin.test.*

val RouterSocketHandlerTests by testSuite {
    val factory = ::RouterSocketHandler

    test(
        "SHALL remove the first frame from each outgoing message " +
            "and use this as the identity of a double queue"
    ) {
        factory.runTest {
            val peers = List(5) { index ->
                val identity = Identity(index.toString().encodeToByteString())

                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peer.identity = identity

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            yield()

            val message = Message { writeFrame("MESSAGE") }

            peers.forEach { peer ->
                val message = Message { writeFrame("MESSAGE") }
                message.pushIdentity(peer.identity!!)
                send(message)
            }

            peers.forEach { peer ->
                assertReceivesExactly(listOf(message), peer.sendChannel)
            }
        }
    }

    testSuite("SHALL route the message to the outgoing queue if that queue exists, and has space") {
        test("matching queue") {
            factory.runTest {
                val peer = PeerMailbox("match", SocketOptions()).also { peer ->
                    peer.identity = Identity("match".encodeToByteString())

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                val message = Message { writeFrame("MESSAGE") }
                message.pushIdentity(Identity("match".encodeToByteString()))
                send(message)

                assertReceivesExactly(listOf(Message { writeFrame("MESSAGE") }), peer.sendChannel)
            }
        }

        test("no matching queue (mandatory=false)") {
            factory.runTest {
                val peer = PeerMailbox("match", SocketOptions()).also { peer ->
                    peer.identity = Identity("match".encodeToByteString())

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                val message = Message { writeFrame("MESSAGE") }
                message.pushIdentity(Identity("no-match".encodeToByteString()))
                send(message)

                assertReceivesNothing(peer.sendChannel)
            }
        }

        test("no matching queue (mandatory=true)") {
            factory.runTest {
                socketOptions.mandatory = true

                val peer = PeerMailbox("match", socketOptions).also { peer ->
                    peer.identity = Identity("match".encodeToByteString())

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                assertFailsWith<IllegalStateException> {
                    val message = Message { writeFrame("MESSAGE") }
                    message.pushIdentity(Identity("no-match".encodeToByteString()))
                    send(message)
                }

                assertReceivesNothing(peer.sendChannel)
            }
        }

        test("no space") {
            factory.runTest {
                val identity = Identity("match".encodeToByteString())
                val peer = PeerMailbox("match", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peer.identity = identity

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(Message { writeFrame("Queued") }))

                val message = Message { writeFrame("Waiting") }

                // Trigger an asynchronous sending
                val result = async {
                    message.pushIdentity(peer.identity!!)
                    send(message)
                }

                // The sending is not suspending and returns
                val awaitResult = assertDoesNotSuspend { result.await() }
                assertEquals(Unit, awaitResult)

                // But the message has been dropped
                assertReceivesExactly(listOf(Message { writeFrame("Queued") }), peer.sendChannel)
            }
        }
    }

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        factory.runTest {
            val peers = List(5) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            yield()

            val messages = List(10) { index -> Message(ByteString(index.toByte())) }

            peers.forEach { peer ->
                messages.forEach { peer.receiveChannel.send(it) }
            }

            val receiveWithoutIdentity = suspend {
                val message = receive()
                message.popIdentity()
                message
            }

            messages.forEach { message ->
                assertReceivesExactly(List(peers.size) { message }, receiveWithoutIdentity)
            }
        }
    }

    suspendingReceiveTests(
        factory = { factory(SocketOptions()) },
        modifyReceivedMessage = { message ->
            message.popIdentity()
        }
    )
}
