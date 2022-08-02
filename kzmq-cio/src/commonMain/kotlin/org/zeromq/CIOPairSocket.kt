/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

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
 * B. For processing incoming messages:
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
    engineInstance: CIOEngineInstance,
) : CIOSocket(engineInstance, Type.PAIR), CIOReceiveSocket, CIOSendSocket, PairSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes

    override val receiveChannel = Channel<Message>()
    override val sendChannel = Channel<Message>()

    init {
        launch {
            var forwardJob: Job? = null

            while (isActive) {
                val (kind, peerMailbox) = peerEvents.receive()
                when (kind) {
                    PeerEvent.Kind.ADDITION -> {
                        // FIXME what should we do if it already has a peer?
                        if (forwardJob != null) continue

                        logger.d { "Peer added: $peerMailbox" }
                        forwardJob = forwardJob(peerMailbox)
                    }

                    PeerEvent.Kind.REMOVAL -> {
                        if (forwardJob == null) continue

                        logger.d { "Peer removed: $peerMailbox" }
                        forwardJob.cancel()
                        forwardJob = null
                    }

                    else -> {}
                }
            }
        }
    }

    private fun forwardJob(mailbox: PeerMailbox) = launch {
        launch {
            while (isActive) {
                val message = sendChannel.receive()
                logger.d { "Sending $message to $mailbox" }
                mailbox.sendChannel.send(CommandOrMessage(message))
            }
        }
        launch {
            while (isActive) {
                val commandOrMessage = mailbox.receiveChannel.receive()
                val message = commandOrMessage.messageOrThrow()
                logger.d { "Receiving $message from $mailbox" }
                receiveChannel.send(message)
            }
        }
    }

    companion object {
        private val validPeerSocketTypes = setOf(Type.PAIR)
    }
}
