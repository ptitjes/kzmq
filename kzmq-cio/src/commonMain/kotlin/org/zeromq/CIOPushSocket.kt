/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

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
    engine: CIOEngine,
) : CIOSocket(engine, Type.PUSH), CIOSendSocket, PushSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes

    override val sendChannel = Channel<Message>()

    init {
        setHandler { handlePushSocket(peerEvents, sendChannel) }
    }

    override var conflate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    companion object {
        private val validPeerSocketTypes = setOf(Type.PULL)
    }
}

internal suspend fun handlePushSocket(
    peerEvents: ReceiveChannel<PeerEvent>,
    sendChannel: ReceiveChannel<Message>,
) = coroutineScope {
    val mailboxes = CircularQueue<PeerMailbox>()

    while (isActive) {
        select {
            peerEvents.onReceive(mailboxes::update)

            if (mailboxes.isNotEmpty()) {
                sendChannel.onReceive { message ->
                    // Fast path: Find the first mailbox we can send immediately
                    logger.v { "Try send message to first available" }
                    val sent = mailboxes.trySendToFirstAvailable(message)

                    if (!sent) {
                        // Slow path: Biased select on each mailbox's onSend
                        logger.v { "Send message to first available" }
                        select {
                            peerEvents.onReceive(mailboxes::update)

                            val commandOrMessage = CommandOrMessage(message)
                            mailboxes.forEachIndexed { index, mailbox ->
                                mailbox.sendChannel.onSend(commandOrMessage) {
                                    logger.v { "Sent message to $mailbox" }
                                    mailboxes.rotateAfter(index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun CircularQueue<PeerMailbox>.update(event: PeerEvent) {
    val mailbox = event.peerMailbox
    when (event.kind) {
        PeerEvent.Kind.ADDITION -> add(mailbox)
        PeerEvent.Kind.REMOVAL -> remove(mailbox)
        else -> {}
    }
}

internal fun CircularQueue<PeerMailbox>.trySendToFirstAvailable(message: Message): Boolean {
    val commandOrMessage = CommandOrMessage(message)
    val index = indexOfFirst { mailbox ->
        val result = mailbox.sendChannel.trySend(commandOrMessage)
        logger.v {
            if (result.isSuccess) "Sent message to $mailbox"
            else "Failed to send message to $mailbox"
        }
        result.isSuccess
    }

    val sent = index != -1
    if (sent) rotateAfter(index)
    return sent
}
