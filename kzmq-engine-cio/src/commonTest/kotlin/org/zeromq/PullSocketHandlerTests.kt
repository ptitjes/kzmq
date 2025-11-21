/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.core.*
import io.kotest.assertions.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

val PullSocketHandlerTests by testSuite {
    suspend fun TestExecutionScope.withHandler(test: SocketHandlerTest) =
        withSocketHandler(PullSocketHandler(), test)

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        withHandler { peerEvents, _, receive ->
            val peers = List(5) { index ->
                PeerMailbox(index.toString(), SocketOptions()).also { peer ->
                    peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                    peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
                }
            }

            val messages = messages(10) { index ->
                writeFrame { writeByte(index.toByte()) }
            }

            peers.forEach { peer ->
                messages.forEach { peer.receiveChannel.send(it) }
            }

            all {
                messages.forEach { message ->
                    receive shouldReceiveExactly List(peers.size) { message }
                }
            }
        }
    }

    test("SHALL deliver these to its calling application") {
        withHandler { peerEvents, _, receive ->
            val peer = PeerMailbox("peer", SocketOptions()).also { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = messages(10) { index ->
                writeFrame { writeByte(index.toByte()) }
            }

            messages.forEach { peer.receiveChannel.send(it) }

            receive shouldReceiveExactly messages
        }
    }
}
