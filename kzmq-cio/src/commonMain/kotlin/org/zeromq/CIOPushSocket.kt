/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.ktor.network.selector.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import kotlin.coroutines.*

/**
 * An implementation of the [PUSH socket](https://rfc.zeromq.org/spec/30/).
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
 * 1. MAY be connected to any number of PULL peers, and SHALL only send messages.
 * 2. SHALL not filter or modify outgoing messages in any way.
 * 3. SHALL maintain an outgoing message queue for each connected peer.
 * 4. SHALL create this queue when initiating an outgoing connection to a peer, and SHALL maintain
 *    the queue whether or not the connection is established.
 * 5. SHALL create this queue when a peer connects to it. If this peer disconnects, the PUSH socket
 *    SHALL destroy its queue and SHALL discard any messages it contains.
 * 6. SHOULD constrain queue sizes to a runtime-configurable limit.
 *
 * B. For processing outgoing messages:
 * 1. SHALL consider a peer as available only when it has a outgoing queue that is not full.
 * 2. SHALL route outgoing messages to available peers using a round-robin strategy.
 * 3. SHALL block on sending, or return a suitable error, when it has no available peers.
 * 4. SHALL not accept further messages when it has no available peers.
 * 5. SHALL NOT discard messages that it cannot queue.
 */
internal class CIOPushSocket(
    coroutineContext: CoroutineContext,
    selectorManager: SelectorManager
) : CIOSocket(coroutineContext, selectorManager, Type.PUSH, setOf(Type.PULL)),
    CIOSendSocket,
    PushSocket {

    override fun createPeerMessageHandler(): MessageHandler = NoopMessageHandler

    override val sendChannel = Channel<Message>()

    init {
        launch(CoroutineName("zmq-push")) {
            val peerMailboxes = hashSetOf<PeerMailbox>()
            val forwardJobs = JobMap<PeerMailbox>()

            while (isActive) {
                val (kind, peerMailbox) = peerEvents.receive()
                when (kind) {
                    PeerEventKind.ADDITION -> {
                        peerMailboxes.add(peerMailbox)
                        logger.d { "Peer added: $peerMailbox" }
                        forwardJobs.add(peerMailbox) { forwardTo(peerMailbox) }
                    }
                    PeerEventKind.REMOVAL -> {
                        peerMailboxes.remove(peerMailbox)
                        logger.d { "Peer removed: $peerMailbox" }
                        forwardJobs.remove(peerMailbox)
                    }
                }
            }
        }
    }

    private fun forwardTo(peerMailbox: PeerMailbox) = launch {
        while (isActive) {
            val message = sendChannel.receive()
            logger.d { "Sending $message to $peerMailbox" }
            peerMailbox.sendChannel.send(CommandOrMessage(message))
        }
    }

    override var conflate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
}

