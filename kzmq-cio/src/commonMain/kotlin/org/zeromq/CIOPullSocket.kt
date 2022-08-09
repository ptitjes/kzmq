/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

/**
 * An implementation of the [PULL socket](https://rfc.zeromq.org/spec/30/).
 *
 * ## The Pipeline Pattern
 *
 * The implementation SHOULD follow [http://rfc.zeromq.org/spec:30/PIPELINE] for the semantics of
 * PUSH and PULL sockets.
 *
 * ## Overall Goals of this Pattern
 *
 * The pattern is intended for task distribution, typically in a multi-stage pipeline where one or
 * a few nodes push work to many workers, and they in turn push results to one or a few collectors.
 * The pattern is mostly reliable insofar as it will not discard messages unless a node disconnects
 * unexpectedly. It is scalable in that nodes can join at any time.
 *
 * ## The PUSH Socket Type
 *
 * The PUSH socket type talks to a set of anonymous PULL peers, sending messages using a
 * round-robin algorithm.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of PUSH peers, and SHALL only receive messages.
 * 2. SHALL not filter or modify incoming messages in any way.
 * 3. SHALL maintain an incoming queue for each connected peer.
 * 4. SHALL create this queue when initiating an outgoing connection to a peer, and SHALL maintain
 *    the queue whether or not the connection is established.
 * 5. SHALL create this queue when a peer connects to it. If this peer disconnects, the PULL socket
 *    SHALL destroy its queue and SHALL discard any messages it contains.
 * 6. SHOULD constrain incoming queue sizes to a runtime-configurable limit.
 *
 * B. For processing incoming messages:
 * 1. SHALL receive incoming messages from its peers using a fair-queuing strategy.
 * 2. SHALL deliver these to its calling application.
 */
internal class CIOPullSocket(
    engineInstance: CIOEngineInstance,
) : CIOSocket(engineInstance, Type.PULL), CIOReceiveSocket, PullSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes

    override val receiveChannel = Channel<Message>()

    init {
        setHandler {
            val forwardJobs = JobMap<PeerMailbox>()

            while (isActive) {
                val (kind, peerMailbox) = peerEvents.receive()
                when (kind) {
                    PeerEvent.Kind.ADDITION -> forwardJobs.add(peerMailbox) { forwardFrom(peerMailbox) }
                    PeerEvent.Kind.REMOVAL -> forwardJobs.remove(peerMailbox)
                    else -> {}
                }
            }
        }
    }

    private fun CoroutineScope.forwardFrom(peerMailbox: PeerMailbox) = launch {
        try {
            while (isActive) {
                val message = peerMailbox.receiveChannel.receive().messageOrThrow()
                logger.t { "Receiving $message from $peerMailbox" }
                receiveChannel.send(message)
            }
        } catch (e: ClosedReceiveChannelException) {
            // Coroutine's cancellation happened while suspending on receive
            // and the receiveChannel of the peerMailbox has already been closed
        }
    }

    override var conflate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    companion object {
        private val validPeerSocketTypes = setOf(Type.PUSH)
    }
}
