/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

val PullSocketHandlerTests by testSuite {
    val factory = ::PullSocketHandler

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        factory.runTest {
            val peers = List(5) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            val messages = List(10) { index -> Message(ByteString(index.toByte())) }

            peers.forEach { peer ->
                messages.forEach { peer.receiveChannel.send(it) }
            }

            messages.forEach { message ->
                assertReceivesExactly(List(peers.size) { message }, ::receive)
            }
        }
    }

    test("SHALL deliver these to its calling application") {
        factory.runTest {
            val peer = PeerMailbox("peer", SocketOptions()).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = List(10) { index -> Message(ByteString(index.toByte())) }

            messages.forEach { peer.receiveChannel.send(it) }

            assertReceivesExactly(messages, ::receive)
        }
    }

    suspendingReceiveTests(factory)
}
