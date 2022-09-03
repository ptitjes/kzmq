/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import io.kotest.core.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import org.zeromq.utils.*

class PullSocketHandlerTests : FunSpec({

    test("SHALL receive incoming messages from its peers using a fair-queuing strategy") {
        withHandler { peerEvents, receiveChannel ->
            val peers = List(5) { index -> PeerMailbox(index.toString(), SocketOptions()) }

            peers.forEach { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = List(10) { index -> Message(ByteArray(1) { index.toByte() }) }

            peers.forEach { peer ->
                messages.forEach { message ->
                    peer.receiveChannel.send(CommandOrMessage(message))
                }
            }

            all {
                messages.forEach { message ->
                    receiveChannel shouldReceiveExactly List(peers.size) { message }
                }
            }
        }
    }
})

private suspend fun TestScope.withHandler(
    block: suspend TestScope.(
        peerEvents: SendChannel<PeerEvent>,
        receiveChannel: ReceiveChannel<Message>,
    ) -> Unit,
) = coroutineScope {
    val peerEvents = Channel<PeerEvent>()
    val receiveChannel = Channel<Message>()

    val handlerJob = launch { handlePullSocket(peerEvents, receiveChannel) }

    block(peerEvents, receiveChannel)

    handlerJob.cancelAndJoin()
}
