/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.ktor.network.selector.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import kotlin.coroutines.*

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
    coroutineContext: CoroutineContext,
    selectorManager: SelectorManager,
) : CIOSocket(coroutineContext, selectorManager, Type.REQ, setOf(Type.REP, Type.ROUTER)),
    CIOSendSocket,
    CIOReceiveSocket,
    RequestSocket {

    override val sendChannel = Channel<Message>()
    override val receiveChannel = Channel<Message>()

    private val requestsChannel = Channel<Pair<PeerMailbox, Message>>()
    private val repliesChannel = Channel<Pair<PeerMailbox, Message>>()

    init {
        launch(CoroutineName("zmq-req-peers")) {
            val peerMailboxes = hashSetOf<PeerMailbox>()
            val forwardJobs = JobMap<PeerMailbox>()

            while (isActive) {
                val (kind, peerMailbox) = peerEvents.receive()
                when (kind) {
                    PeerEventKind.ADDITION -> {
                        peerMailboxes.add(peerMailbox)
                        logger.d { "Peer added: $peerMailbox" }
                        forwardJobs.add(peerMailbox) { dispatchRequestsReplies(peerMailbox) }
                    }

                    PeerEventKind.REMOVAL -> {
                        peerMailboxes.remove(peerMailbox)
                        logger.d { "Peer removed: $peerMailbox" }
                        forwardJobs.remove(peerMailbox)
                    }
                }
            }
        }
        launch {
            while (isActive) {
                val (peerMailbox, requestData) = requestsChannel.receive()

                val request = prependAddress(requestData)
                logger.d { "Sending request $request to $peerMailbox" }
                peerMailbox.sendChannel.send(CommandOrMessage(request))

                while (isActive) {
                    val (otherPeerMailbox, reply) = repliesChannel.receive()
                    if (otherPeerMailbox != peerMailbox) {
                        logger.d { "Ignoring reply $reply from $otherPeerMailbox" }
                        continue
                    }

                    logger.d { "Sending back reply $reply from $peerMailbox" }
                    val (_, replyData) = extractAddress(reply)
                    receiveChannel.send(replyData)
                    break
                }
            }
        }
    }

    private fun dispatchRequestsReplies(peerMailbox: PeerMailbox) = launch {
        launch {
            while (isActive) {
                val request = sendChannel.receive()
                logger.d { "Dispatching request $request to $peerMailbox" }
                requestsChannel.send(peerMailbox to request)
            }
        }
        launch {
            while (isActive) {
                val reply = peerMailbox.receiveChannel.receive().messageOrThrow()
                logger.d { "Dispatching reply $reply from $peerMailbox" }
                repliesChannel.send(peerMailbox to reply)
            }
        }
    }

    override var routingId: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var probeRouter: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var correlate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var relaxed: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
}
