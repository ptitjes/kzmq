/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import io.kotest.core.test.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import org.zeromq.utils.*
import kotlin.time.Duration.Companion.seconds

class PushSocketHandlerTests : FunSpec({

    test("SHALL consider a peer as available only when it has an outgoing queue that is not full") {
        withHandler { peerEvents, sendChannel ->
            val peer1 = PeerMailbox("1", SocketOptions())
            val peer2 = PeerMailbox("2", SocketOptions().apply { sendQueueSize = 5 })

            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer1))
            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer2))

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer1))

            val firstBatch = List(5) { index -> Message(ByteArray(1) { index.toByte() }) }
            val secondBatch = List(10) { index -> Message(ByteArray(1) { (index + 10).toByte() }) }

            // Send each message of the first batch once per receiver
            firstBatch.forEach { message -> repeat(2) { sendChannel.send(message) } }
            // Send each message of the second batch once
            secondBatch.forEach { message -> sendChannel.send(message) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer2))

            all {
                peer1.sendChannel shouldReceiveExactly firstBatch + secondBatch
                peer2.sendChannel shouldReceiveExactly firstBatch
            }
        }
    }

    test("SHALL route outgoing messages to available peers using a round-robin strategy") {
        withHandler { peerEvents, sendChannel ->
            val peers = List(5) { index -> PeerMailbox(index.toString(), SocketOptions()) }

            peers.forEach { peer ->
                peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))
                peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))
            }

            val messages = List(10) { index -> Message(ByteArray(1) { index.toByte() }) }

            // Send each message once per receiver
            messages.forEach { message -> repeat(peers.size) { sendChannel.send(message) } }

            all {
                // Check each receiver got every messages
                peers.forEach { peer -> peer.sendChannel shouldReceiveExactly messages }
            }
        }
    }

    test("SHALL suspend on sending when it has no available peers") {
        withHandler { _, sendChannel ->
            val message = Message("Won't be sent".encodeToByteArray())

            withTimeoutOrNull(1.seconds) {
                sendChannel.send(message)
            } shouldBe null
        }
    }

    test("SHALL not accept further messages when it has no available peers") {
        withHandler { _, sendChannel ->
            val message = Message("Won't be sent".encodeToByteArray())

            withTimeoutOrNull(1.seconds) {
                sendChannel.send(message)
            } shouldBe null
        }
    }

    test("SHALL NOT discard messages that it cannot queue") {
        withHandler { peerEvents, sendChannel ->
            val peer = PeerMailbox("1", SocketOptions())
            peerEvents.send(PeerEvent(PeerEvent.Kind.ADDITION, peer))

            val messages = List(10) { index -> Message(ByteArray(1) { index.toByte() }) }

            // Send each message once
            messages.forEach { message -> sendChannel.send(message) }

            peerEvents.send(PeerEvent(PeerEvent.Kind.CONNECTION, peer))

            // Check each receiver got every messages
            peer.sendChannel shouldReceiveExactly messages
        }
    }
})

private suspend fun TestScope.withHandler(
    block: suspend TestScope.(
        peerEvents: SendChannel<PeerEvent>,
        sendChannel: SendChannel<Message>,
    ) -> Unit,
) = coroutineScope {
    val peerEvents = Channel<PeerEvent>()
    val sendChannel = Channel<Message>()

    val handlerJob = launch { handlePushSocket(peerEvents, sendChannel) }

    block(peerEvents, sendChannel)

    handlerJob.cancelAndJoin()
}
