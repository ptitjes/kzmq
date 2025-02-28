/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

internal class RouterSocketHandlerTests : FunSpec({
    val factory = ::RouterSocketHandler

    test(
        "SHALL remove the first frame from each outgoing message " +
            "and use this as the identity of a double queue"
    ) {
        factory.runTest { peerEvents, send, receive ->
            val peers = List(5) { index ->
                val identity = Identity(index.toString().encodeToByteString())

                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peer.identity = identity

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            yield()

            val message = message { writeFrame("MESSAGE") }

            peers.forEach { peer ->
                val message = buildMessage { writeFrame("MESSAGE") }
                message.pushIdentity(peer.identity!!)
                send(message)
            }

            peers.forEach { peer ->
                peer.sendChannel shouldReceiveExactly listOf(message)
            }
        }
    }

    testSet("SHALL route the message to the outgoing queue if that queue exists, and has space") {
        test("matching queue") {
            factory.runTest { peerEvents, send, receive ->
                val peer = PeerMailbox("match", SocketOptions()).also { peer ->
                    peer.identity = Identity("match".encodeToByteString())

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                val message = buildMessage { writeFrame("MESSAGE") }
                message.pushIdentity(Identity("match".encodeToByteString()))
                send(message)

                peer.sendChannel shouldReceiveExactly listOf(message { writeFrame("MESSAGE") })
            }
        }

        test("no matching queue") {
            factory.runTest { peerEvents, send, receive ->
                val peer = PeerMailbox("match", SocketOptions()).also { peer ->
                    peer.identity = Identity("match".encodeToByteString())

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                val message = buildMessage { writeFrame("MESSAGE") }
                message.pushIdentity(Identity("no-match".encodeToByteString()))
                send(message)

                peer.sendChannel.shouldReceiveNothing()
            }
        }

        test("no space") {
            factory.runTest { peerEvents, send, receive ->
                val identity = Identity("match".encodeToByteString())
                val peer = PeerMailbox("match", SocketOptions().apply { sendQueueSize = 1 }).also { peer ->
                    peer.identity = identity

                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }

                yield()

                // Fill the peer's send queue
                peer.sendChannel.send(CommandOrMessage(buildMessage { writeFrame("Queued") }))

                val message = buildMessage { writeFrame("Waiting") }

                // Trigger an asynchronous sending
                val result = async {
                    message.pushIdentity(peer.identity!!)
                    send(message)
                }

                // The sending is not suspending and returns
                shouldNotSuspend { result.await() } shouldBe Unit

                // But the message has been dropped
                peer.sendChannel shouldReceiveExactly listOf(message { writeFrame("Queued") })
            }
        }
    }

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        factory.runTest { peerEvents, _, receive ->
            val peers = List(5) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            yield()

            val messages = messages(10) { index ->
                writeFrame { writeByte(index.toByte()) }
            }

            peers.forEach { peer ->
                messages.forEach { peer.receiveChannel.send(it) }
            }

            val receiveWithoutIdentity = suspend {
                val message = receive()
                message.popIdentity()
                message
            }

            all {
                messages.forEach { message ->
                    receiveWithoutIdentity shouldReceiveExactly List(peers.size) { message }
                }
            }
        }
    }

    suspendingReceiveTests(
        factory = factory,
        modifyReceivedMessage = { message ->
            message.popIdentity()
        }
    )
})
