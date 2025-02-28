/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*

/**
 * An implementation of the [PAIR socket](https://rfc.zeromq.org/spec/31/).
 *
 * ## The Exclusive Pair Pattern
 *
 * The implementation SHOULD follow [http://rfc.zeromq.org/spec:31/EXPAIR] for the semantics of
 * exclusive PAIR sockets.
 *
 * ## Overall Goals of this Pattern
 *
 * PAIR is not a general-purpose socket but is intended for specific use cases where the two peers
 * are architecturally stable. This usually limits PAIR to use within a single process, for
 * inter-thread communication.
 *
 * ## The PUSH Socket Type
 *
 * A. General behavior:
 * 1. MAY be connected to at most one PAIR peers, and MAY both send and receive messages.
 * 2. SHALL not filter or modify outgoing or incoming messages in any way.
 * 3. SHALL maintain a double queue for its peer, allowing outgoing and incoming messages to be
 *    queued independently.
 * 4. SHALL create a double queue when initiating an outgoing connection to a peer, and SHALL
 *    maintain the double queue whether or not the connection is established.
 * 5. SHALL create a double queue when a peer connects to it. If this peer disconnects, the PAIR
 *    socket SHALL destroy its double queue and SHALL discard any messages it contains.
 * 6. SHOULD constrain incoming and outgoing queue sizes to a runtime-configurable limit.
 *
 * B. For processing outgoing messages:
 * 1. SHALL consider its peer as available only when it has a outgoing queue that is not full.
 * 2. SHALL block on sending, or return a suitable error, when it has no available peer.
 * 3. SHALL not accept further messages when it has no available peer.
 * 4. SHALL NOT discard messages that it cannot queue.
 *
 * C. For processing incoming messages:
 * 1. SHALL receive incoming messages from its single peer if it has one.
 * 2. SHALL deliver these to its calling application.
 */
internal class CIOPairSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.PAIR), CIOReceiveSocket, CIOSendSocket, PairSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(PairSocketHandler(options))

    companion object {
        private val validPeerSocketTypes = setOf(Type.PAIR)
    }
}

internal class PairSocketHandler(private val options: SocketOptions) : SocketHandler {
    private val mailbox = atomic<PeerMailbox?>(null)

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            val (kind, peerMailbox) = peerEvents.receive()
            when (kind) {
                PeerEvent.Kind.ADDITION -> mailbox.value = peerMailbox
                PeerEvent.Kind.REMOVAL -> mailbox.value = null
                else -> {}
            }
        }
    }

    override suspend fun send(message: Message) {
        while (true) {
            val result = trySend(message)
            if (result != null) return
            yield()
        }
    }

    override fun trySend(message: Message): Unit? {
        val maybeMailbox = mailbox.value
        if (maybeMailbox != null) {
            val result = maybeMailbox.sendChannel.trySend(CommandOrMessage(message))
            if (result.isSuccess) {
                logger.v { "Sent message to $maybeMailbox" }
                return Unit
            }
        }
        return null
    }

    override suspend fun receive(): Message {
        while (true) {
            val result = tryReceive()
            if (result != null) return result
            yield()
        }
    }

    override fun tryReceive(): Message? {
        val mailbox = mailbox.value
        if (mailbox != null) {
            val result = mailbox.receiveChannel.tryReceive()
            if (result.isSuccess) {
                val message = result.getOrThrow().messageOrThrow()
                logger.v { "Receiving $message from $mailbox" }
                return message
            }
        }
        return null
    }
}
