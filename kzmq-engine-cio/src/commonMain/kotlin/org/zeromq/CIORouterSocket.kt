/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

/**
 * An implementation of the [ROUTER socket](https://rfc.zeromq.org/spec/28/).
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
 * ## The ROUTER Socket Type
 *
 * The ROUTER socket type talks to a set of peers, using explicit addressing so that each outgoing
 * message is sent to a specific peer connection. ROUTER works as an asynchronous replacement for
 * REP, and is often used as the basis for servers that talk to DEALER clients.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of REQ, DEALER, or ROUTER peers, and MAY both send and
 *    receive messages.
 * 2. SHALL maintain a double queue for each connected peer, allowing outgoing and incoming
 *    messages to be queued independently.
 * 3. SHALL create a double queue when initiating an outgoing connection to a peer, and SHALL
 *    maintain the double queue whether or not the connection is established.
 * 4. SHALL create a double queue when a peer connects to it. If this peer disconnects, the ROUTER
 *    socket SHALL destroy its double queue and SHALL discard any messages it contains.
 * 5. SHALL identify each double queue using a unique “identity” binary string.
 * 6. SHOULD allow the peer to specify its identity explicitly through the Identity metadata
 *    property.
 * 7. SHOULD constrain incoming and outgoing queue sizes to a runtime-configurable limit..
 *
 * B. For processing incoming messages:
 * 1. SHALL receive incoming messages from its peers using a fair-queuing strategy.
 * 2. SHALL prefix each incoming message with a frame containing the identity of the originating
 *    double queue.
 * 3. SHALL deliver the resulting messages to its calling application.
 *
 * C. For processing outgoing messages:
 * 1. SHALL remove the first frame from each outgoing message and use this as the identity of a
 *    double queue.
 * 2. SHALL route the message to the outgoing queue if that queue exists, and has space.
 * 3. SHALL either silently drop the message, or return an error, depending on configuration, if
 *    the queue does not exist, or is full.
 * 4. SHALL NOT block on sending.
 */
internal class CIORouterSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.ROUTER), CIOReceiveSocket, CIOSendSocket, RouterSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(RouterSocketHandler(options))

    override var routingId: ByteString? by options::routingId
    override var probeRouter: Boolean by notImplementedOption("Not yet implemented")
    override var mandatory: Boolean by notImplementedOption("Not yet implemented")
    override var handover: Boolean by notImplementedOption("Not yet implemented")

    companion object {
        private val validPeerSocketTypes = setOf(Type.REQ, Type.DEALER, Type.ROUTER)
    }
}

internal class RouterSocketHandler(private val options: SocketOptions) : SocketHandler {
    private val mailboxes = CircularQueue<PeerMailbox>()
    private val perIdentityMailboxes = hashMapOf<Identity, PeerMailbox>()

    private fun randomIdentity(): Identity {
        while (true) {
            val identity = Identity.random()
            if (identity !in perIdentityMailboxes) return identity
        }
    }

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            val event = peerEvents.receive()

            mailboxes.updateOnAdditionRemoval(event)

            val (kind, mailbox) = event
            when (kind) {
                PeerEvent.Kind.CONNECTION -> {
                    val identity = mailbox.identity ?: randomIdentity().also { mailbox.identity = it }
                    perIdentityMailboxes[identity] = mailbox
                    logger.d { "Assigned identity $identity to $mailbox" }
                }

                PeerEvent.Kind.DISCONNECTION -> {
                    val identity = mailbox.identity ?: error("Peer identity should not be null")
                    perIdentityMailboxes.remove(identity)
                    logger.d { "Removed identity $identity from $mailbox" }
                }

                else -> {}
            }
        }
    }

    override suspend fun send(message: Message) {
        trySend(message)
    }

    override fun trySend(message: Message): Unit? {
        val toSend = message.copy()
        val identity = toSend.popIdentity()
        return perIdentityMailboxes[identity]?.let { mailbox ->
            logger.d { "Forwarding reply $toSend to $mailbox with identity $identity" }
            mailbox.sendChannel.trySend(CommandOrMessage(toSend)).getOrNull()
        }
    }

    override suspend fun receive(): Message {
        return mailboxes.receiveFromFirst().messageWithIdentity()
    }

    override fun tryReceive(): Message? {
        return mailboxes.tryReceiveFromFirst()?.messageWithIdentity()
    }

    private fun Receipt.messageWithIdentity(): Message {
        val identity = sourceMailbox.identity ?: error("Peer identity should not be null")
        val message = commandOrMessage.messageOrThrow()
        message.pushIdentity(identity)
        return message
    }
}
