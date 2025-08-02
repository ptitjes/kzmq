/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.io.bytestring.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

val PairSocketHandlerTests by testSuite {
    val factory = ::PairSocketHandler

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        factory.runTest {
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
            }

            val messages = List(5) { index -> Message(ByteString(index.toByte())) }

            // Send each message of the first batch once
            messages.forEach { send(it) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            assertReceivesExactly(messages, peer.sendChannel)
        }
    }

    suspendingSendTests(factory)

    test("SHALL receive incoming messages from its single peer if it has one") {
        factory.runTest {
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = List(5) { index -> Message(ByteString(index.toByte())) }

            // Send each message of the first batch once
            messages.forEach { peer.receiveChannel.send(CommandOrMessage(it)) }

            assertReceivesExactly(messages, ::receive)
        }
    }

    suspendingReceiveTests(factory)
}
