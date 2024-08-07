/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

/**
 * An implementation of the [REP socket](https://rfc.zeromq.org/spec/28/).
 *
 * ## The Request-Reply Pattern
 *
 * The implementation SHOULD follow [http://rfc.zeromq.org/spec:28/REQREP] for the semantics of
 * REQ, REP, DEALER and ROUTER sockets.
 *
 * ## Overall Goals of this Pattern
 *
 * The request-reply pattern is intended for service-oriented architectures of various kinds. It
 * comes in two basic flavors: synchronous (REQ and REP), and asynchronous (DEALER and ROUTER),
 * which may be mixed in various ways. The DEALER and ROUTER sockets are building blocks for many
 * higher-level protocols such as [rfc.zeromq.org/spec:18/MDP].
 *
 * ## The REP Socket Type
 *
 * The REP socket type acts as as service for a set of client peers, receiving requests and sending
 * replies back to the requesting peers. It is designed for simple remote-procedure call models.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of REQ or DEALER peers.
 * 2. SHALL not filter or modify outgoing or incoming messages in any way.
 * 3. SHALL receive and then send exactly one message at a time.
 *
 * B. The request and reply messages SHALL have this format on the wire:
 * 1. An address envelope consisting of zero or more frames, each containing one identity.
 * 2. A delimiter, consisting of an empty frame.
 * 3. One or more data frames, comprising the message visible to the application.
 *
 * C. For processing incoming messages:
 * 1. SHALL receive incoming messages from its peers using a fair-queuing strategy.
 * 2. SHALL remove and store the address envelope, including the delimiter.
 * 3. SHALL pass the remaining data frames to its calling application.
 *
 * D. For processing outgoing messages:
 * 1. SHALL wait for a single reply message from its calling application.
 * 2. SHALL prepend the address envelope and delimiter.
 * 3. SHALL deliver this message back to the originating peer.
 * 4. SHALL silently discard the reply, or return an error, if the originating peer is no longer
 *    connected.
 * 5. SHALL not block on sending.
 */
internal class CIOReplySocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.REP), CIOReceiveSocket, CIOSendSocket, ReplySocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(ReplySocketHandler())

    override var routingId: ByteString? by options::routingId

    companion object {
        private val validPeerSocketTypes = setOf(Type.REQ, Type.DEALER)
    }
}

internal class ReplySocketHandler : SocketHandler {
    private val mailboxes = CircularQueue<PeerMailbox>()
    private var state = atomic<ReplySocketState>(ReplySocketState.Idle)
    private val requestReplyLock = Mutex()

    private suspend fun awaitState(predicate: (ReplySocketState?) -> Boolean) {
        while (!predicate(state.value)) yield()
    }

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            val event = peerEvents.receive()
            mailboxes.updateOnAdditionRemoval(event)
        }
    }

    override suspend fun receive(): Message {
        awaitState { it is ReplySocketState.Idle }
        requestReplyLock.withLock {
            val (mailbox, message) = mailboxes.receiveFromFirst()
            state.value = ReplySocketState.ProcessingRequest(mailbox, message.popPrefixAddress())
            return message
        }
    }

    override suspend fun send(message: Message) {
        awaitState { it is ReplySocketState.ProcessingRequest }
        requestReplyLock.withLock {
            val (peer, address) = state.value as ReplySocketState.ProcessingRequest

            message.pushPrefixAddress(address)
            peer.sendChannel.send(CommandOrMessage(message))
            state.value = ReplySocketState.Idle
        }
    }
}

private sealed interface ReplySocketState {
    data object Idle : ReplySocketState

    data class ProcessingRequest(
        val peer: PeerMailbox,
        val address: List<ByteString>,
    ) : ReplySocketState
}
