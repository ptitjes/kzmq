/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

/**
 * An implementation of the [DEALER socket](https://rfc.zeromq.org/spec/28/).
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
 * ## The DEALER Socket Type
 *
 * The DEALER socket type talks to a set of anonymous peers, sending and receiving messages using
 * round-robin algorithms. It is reliable, insofar as it does not drop messages. DEALER works as
 * an asynchronous replacement for REQ, for clients that talk to REP or ROUTER servers. It is also
 * used in request-reply proxies.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of REP or ROUTER peers, and MAY both send and receive
 *    messages.
 * 2. SHALL not filter or modify outgoing or incoming messages in any way.
 * 3. SHALL maintain a double queue for each connected peer, allowing outgoing and incoming
 *    messages to be queued independently.
 * 4. SHALL create a double queue when initiating an outgoing connection to a peer, and SHALL
 *    maintain the double queue whether or not the connection is established.
 * 5. SHALL create a double queue when a peer connects to it. If this peer disconnects, the DEALER
 *    socket SHALL destroy its double queue and SHALL discard any messages it contains.
 * 6. SHOULD constrain incoming and outgoing queue sizes to a runtime-configurable limit..
 *
 * B. For processing outgoing messages:
 * 1. SHALL consider a peer as available only when it has a outgoing queue that is not full.
 * 2. SHALL route outgoing messages to available peers using a round-robin strategy.
 * 3. SHALL block on sending, or return a suitable error, when it has no available peers.
 * 4. SHALL not accept further messages when it has no available peers.
 * 5. SHALL NOT discard messages that it cannot queue.
 *
 * C. For processing incoming messages:
 * 1. SHALL receive incoming messages from its peers using a fair-queuing strategy.
 * 2. SHALL deliver these to its calling application.
 */
internal class CIODealerSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.DEALER), CIOSendSocket, CIOReceiveSocket, DealerSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(DealerSocketHandler())

    override var conflate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var routingId: ByteString? by options::routingId

    override var probeRouter: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    companion object {
        private val validPeerSocketTypes = setOf(Type.REP, Type.ROUTER)
    }
}

internal class DealerSocketHandler : SocketHandler {
    private val outgoingMailboxes = CircularQueue<PeerMailbox>()
    private val incomingMailboxes = CircularQueue<PeerMailbox>()

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            val event = peerEvents.receive()
            outgoingMailboxes.updateOnAdditionRemoval(event)
            incomingMailboxes.updateOnAdditionRemoval(event)
        }
    }

    override suspend fun send(message: Message) {
        outgoingMailboxes.sendToFirstAvailable(message)
    }

    override suspend fun receive(): Message {
        val (_, message) = incomingMailboxes.receiveFromFirst()
        return message
    }
}
