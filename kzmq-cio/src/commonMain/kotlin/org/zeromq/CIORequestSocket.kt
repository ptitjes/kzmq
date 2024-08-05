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
 * An implementation of the [REQ socket](https://rfc.zeromq.org/spec/28/).
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
 * ## The REQ Socket Type
 *
 * The REQ socket type acts as the client for a set of anonymous services, sending requests and
 * receiving replies using a lock-step round-robin algorithm. It is designed for simple
 * request-reply models where reliability against failing peers is not an issue.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of REP or ROUTER peers.
 * 2. SHALL send and then receive exactly one message at a time.
 *
 * B. The request and reply messages SHALL have this format on the wire:
 * 1. A delimiter, consisting of an empty frame, added by the REQ socket.
 * 2. One or more data frames, comprising the message visible to the application.
 *
 * C. For processing outgoing messages:
 * 1. SHALL prefix the outgoing message with an empty delimiter frame.
 * 2. SHALL route outgoing messages to connected peers using a round-robin strategy.
 * 3. SHALL block on sending, or return a suitable error, when it has no connected peers.
 * 4. SHALL NOT discard messages that it cannot send to a connected peer.
 *
 * D. For processing incoming messages:
 * 1. SHALL accept an incoming message only from the last peer that it sent a request to.
 * 2. SHALL discard silently any messages received from other peers.
 */
internal class CIORequestSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.REQ), CIOSendSocket, CIOReceiveSocket, RequestSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(RequestSocketHandler())

    override var routingId: ByteString? by options::routingId
    override var probeRouter: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var correlate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var relaxed: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    companion object {
        private val validPeerSocketTypes = setOf(Type.REP, Type.ROUTER)
    }
}

internal class RequestSocketHandler : SocketHandler {
    private val mailboxes = CircularQueue<PeerMailbox>()
    private var lastSentPeer = atomic<PeerMailbox?>(null)
    private val requestReplyLock = Mutex()

    private suspend fun awaitLastSentPeer(predicate: (PeerMailbox?) -> Boolean) {
        while (!predicate(lastSentPeer.value)) yield()
    }

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            mailboxes.update(peerEvents.receive())
        }
    }

    override suspend fun send(message: Message) {
        awaitLastSentPeer { it == null }
        requestReplyLock.withLock {
            message.pushPrefixAddress()
            val mailbox = mailboxes.sendToFirstAvailable(message)
            lastSentPeer.value = mailbox
            logger.v { "Sent request to $mailbox" }
        }
    }

    override suspend fun receive(): Message {
        awaitLastSentPeer { it != null }
        requestReplyLock.withLock {
            while (true) {
                val (mailbox, message) = mailboxes.receiveFromFirst()

                message.popPrefixAddress()

                // Should we "discard" messages in another coroutine in `handle()`?
                if (mailbox != lastSentPeer.value) {
                    logger.w { "Ignoring reply $message from $mailbox" }
                    continue
                }

                logger.v { "Received reply $message from $mailbox" }
                lastSentPeer.value = null
                return message
            }
        }
    }
}
