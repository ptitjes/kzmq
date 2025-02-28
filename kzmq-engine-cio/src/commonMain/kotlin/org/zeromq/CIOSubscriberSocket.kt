/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

/**
 * An implementation of the [SUB socket](https://rfc.zeromq.org/spec/29/).
 *
 * ## The Publish-Subscribe Pattern
 *
 * The implementation SHOULD follow [http://rfc.zeromq.org/spec:29/PUBSUB] for the semantics of
 * PUB, XPUB, SUB and XSUB sockets.
 *
 * When using ZMTP, message filtering SHALL happen at the publisher side (the PUB or XPUB socket).
 * To create a subscription, the SUB or XSUB peer SHALL send a SUBSCRIBE command, which has this
 * grammar:
 *
 * > subscribe = command-size %d9 "SUBSCRIBE" subscription
 * > subscription = *OCTET
 *
 * To cancel a subscription, the SUB or XSUB peer SHALL send a CANCEL command, which has this
 * grammar:
 * > cancel = command-size %d6 "CANCEL" subscription
 *
 * The subscription is a binary string that specifies what messages the subscriber wants. A
 * subscription of “A” SHALL match all messages starting with “A”. An empty subscription SHALL
 * match all messages.
 *
 * Subscriptions SHALL be additive and SHALL NOT be idempotent. That is, subscribing to “A” and "”
 * is the same as subscribing to "” alone. Subscribing to “A” and “A” counts as two subscriptions,
 * and would require two CANCEL commands to undo.
 *
 * ## Overall Goals of this Pattern
 *
 * The pattern is intended for event and data distribution, usually from a small number of
 * publishers to a large number of subscribers, but also from many publishers to a few subscribers.
 * For many-to-many use-cases the pattern provides raw socket types (XPUB, XSUB) to construct
 * distribution proxies, also called brokers.
 *
 * The exact subscription and filtering mechanisms depend on the transport protocol and are defined
 * in the relevant documents. For TCP, refer to
 * [http://rfc.zeromq.org/spec:23/ZMTP](http://rfc.zeromq.org/spec:23/ZMTP).
 *
 * ## The SUB Socket Type
 *
 * The SUB socket type provides a basic one-way listener for a set of publishers.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of PUB or XPUB publishers, and SHALL only receive messages.
 * 2. SHALL maintain a single incoming message queue for each connected publisher.
 * 3. SHALL create a queue when initiating an outgoing connection to a publisher, and SHALL
 *    maintain the queue whether or not the connection is established.
 * 4. SHALL create a queue when a publisher connects to it. If this publisher disconnects, the SUB
 *    socket SHALL destroy its queue and SHALL discard any messages it contains.
 * 5. SHOULD constrain queue sizes to a runtime-configurable limit.
 *
 * B. For processing incoming messages:
 * 1. SHALL silently discard messages if the queue for a publisher is full.
 * 2. SHALL receive incoming messages from its publishers using a fair-queuing strategy.
 * 3. SHALL not modify incoming messages in any way.
 * 4. MAY, depending on the transport, filter messages according to subscriptions, using a prefix
 *    match algorithm.
 * 5. SHALL deliver messages to its calling application.
 *
 * C. For processing subscriptions:
 * 1. MAY send subscribe and unsubscribe requests to publishers depending on the transport protocol
 *    used.
 */
internal class CIOSubscriberSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.SUB), CIOReceiveSocket, SubscriberSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(SubscriberSocketHandler(options))

    override suspend fun subscribe() {
        handler.subscriptions.subscribe(listOf())
    }

    override suspend fun subscribe(vararg topics: ByteString) {
        handler.subscriptions.subscribe(topics.toList())
    }

    override suspend fun subscribe(vararg topics: String) {
        handler.subscriptions.subscribe(topics.map { it.encodeToByteString() })
    }

    override suspend fun unsubscribe() {
        handler.subscriptions.unsubscribe(listOf())
    }

    override suspend fun unsubscribe(vararg topics: ByteString) {
        handler.subscriptions.unsubscribe(topics.toList())
    }

    override suspend fun unsubscribe(vararg topics: String) {
        handler.subscriptions.unsubscribe(topics.map { it.encodeToByteString() })
    }

    override var conflate: Boolean by notImplementedOption("Not yet implemented")

    override var invertMatching: Boolean by notImplementedOption("Not yet implemented")

    companion object {
        private val validPeerSocketTypes = setOf(Type.PUB, Type.XPUB)
    }
}

internal class SubscriberSocketHandler(private val options: SocketOptions) : SocketHandler {
    private val mailboxes = CircularQueue<PeerMailbox>()
    val subscriptions = SubscriptionManager()

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            select {
                peerEvents.onReceive { event ->
                    mailboxes.updateOnAdditionRemoval(event)

                    val (kind, mailbox) = event
                    when (kind) {
                        PeerEvent.Kind.ADDITION -> {
                            for (subscription in subscriptions.existing) {
                                logger.d { "Sending subscription $subscription to $mailbox" }
                                mailbox.sendChannel.send(CommandOrMessage(SubscribeCommand(subscription)))
                            }
                        }

                        else -> {}
                    }
                }

                subscriptions.lateSubscriptionCommands.onReceive { command ->
                    for (mailbox in mailboxes) {
                        logger.d { "Sending late subscription $command to $mailbox" }
                        mailbox.sendChannel.send(CommandOrMessage(command))
                    }
                }
            }
        }
    }

    override suspend fun receive(): Message = mailboxes.receiveFromFirst().messageOrThrow()

    override fun tryReceive(): Message? = mailboxes.tryReceiveFromFirst()?.messageOrThrow()

    private fun Receipt.messageOrThrow() = commandOrMessage.messageOrThrow()
}
