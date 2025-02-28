/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.core.spec.style.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

internal class PairSocketHandlerTests : FunSpec({
    val factory = ::PairSocketHandler

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        factory.runTest { peerEvents, send, _ ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
            }

            val messages = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }

            // Send each message of the first batch once
            messages.forEach { send(it.buildMessage()) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            peer.sendChannel shouldReceiveExactly messages
        }
    }

    suspendingSendTests(factory)

    test("SHALL receive incoming messages from its single peer if it has one") {
        factory.runTest { peerEvents, _, receive ->
            val peer = PeerMailbox("peer", SocketOptions().apply { sendQueueSize = 5 }).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = messages(5) { index -> writeFrame { writeByte(index.toByte()) } }

            // Send each message of the first batch once
            messages.forEach { peer.receiveChannel.send(CommandOrMessage(it.buildMessage())) }

            receive shouldReceiveExactly messages
        }
    }

    suspendingReceiveTests(factory)
})
