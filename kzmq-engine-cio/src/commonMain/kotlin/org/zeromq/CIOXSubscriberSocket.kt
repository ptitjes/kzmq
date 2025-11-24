/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*
import org.zeromq.internal.*
import org.zeromq.internal.utils.*

/**
 * An implementation of the [XSUB socket](https://rfc.zeromq.org/spec/29/).
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
 * ## The XSUB Socket Type
 *
 * The XSUB socket type extends the SUB socket with the ability to send messages and subscription
 * commands upstream to publishers. XSUB is usually used in proxies but is also useful for
 * advanced applications.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of PUB or XPUB publishers, and MAY both send and receive
 *    messages.
 * 2. SHALL maintain a double queue for each connected publisher, allowing outgoing and incoming
 *    messages to be queued independently.
 * 3. SHALL create a double queue when initiating an outgoing connection to a publisher, and SHALL
 *    maintain the double queue whether or not the connection is established.
 * 4. SHALL create a double queue when a publisher connects to it. If this publisher disconnects,
 *    the XSUB socket SHALL destroy its double queue and SHALL discard any messages it contains.
 * 5. SHOULD constrain incoming and outgoing queue sizes to a runtime-configurable limit.
 *
 * B. For processing incoming messages:
 * 1. SHALL not modify incoming messages in any way.
 * 2. SHALL silently discard messages if the incoming queue for a publisher is full.
 * 3. SHALL receive incoming messages from its publishers using a fair-queuing strategy.
 * 4. MAY, depending on the transport, filter messages according to subscriptions, using a prefix
 *    match algorithm.
 * 5. SHALL deliver messages to its calling application.
 *
 * C. For processing outgoing messages:
 * 1. SHALL not modify outgoing messages in any way.
 * 2. SHALL send all messages to all connected publishers.
 * 3. SHALL silently drop the message if the outgoing queue for a publisher is full.
 * 4. SHALL NOT block on sending.
 *
 * D. For processing subscriptions:
 * 1. SHALL accept subscription commands from its calling application.
 * 2. SHALL send subscribe and unsubscribe requests to publishers depending on the transport protocol used.
 * 3. When closing a connection to a publisher SHOULD send unsubscribe requests for all subscriptions.
 */
internal class CIOXSubscriberSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.XSUB), CIOSendSocket, CIOReceiveSocket, XSubscriberSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes

    override val handler = setupHandler(XSubscriberSocketHandler(options))

    companion object {
        private val validPeerSocketTypes = setOf(Type.PUB, Type.XPUB)
    }
}

internal class XSubscriberSocketHandler(private val options: SocketOptions) : SocketHandler {
    private val mailboxes = CircularQueue<PeerMailbox>()
    private val subscriptions = SubscriptionManager()

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            select {
                peerEvents.onReceive { event ->
                    mailboxes.updateOnAdditionRemoval(event)

                    val (kind, peerMailbox) = event
                    when (kind) {
                        PeerEvent.Kind.ADDITION -> {
                            for (subscription in subscriptions.existing) {
                                logger.d { "Sending subscription $subscription to $peerMailbox" }
                                peerMailbox.sendChannel.send(CommandOrMessage(SubscribeCommand(subscription)))
                            }
                        }

                        else -> {}
                    }
                }

                subscriptions.lateSubscriptionCommands.onReceive { command ->
                    for (peerMailbox in mailboxes) {
                        logger.d { "Sending late subscription $command to $peerMailbox" }
                        peerMailbox.sendChannel.send(CommandOrMessage(command))
                    }
                }
            }
        }
    }

    override suspend fun send(message: Message) {
        val subscriptionTopicPair = message.toSubscriptionMessage()?.let {
            it.subscribe to it.topic
        }
        if (subscriptionTopicPair != null) {
            val (subscribe, topic) = subscriptionTopicPair
            if (subscribe) subscriptions.subscribe(listOf(topic))
            else subscriptions.unsubscribe(listOf(topic))
        } else {
            mailboxes.forEach { mailbox ->
                logger.v { "Sending message $message to $mailbox" }
                mailbox.sendChannel.send(CommandOrMessage(message))
            }
        }
    }

    override suspend fun receive(): Message = mailboxes.receiveFromFirst().messageOrThrow()

    override fun tryReceive(): Message? = mailboxes.tryReceiveFromFirst()?.messageOrThrow()

    private fun Receipt.messageOrThrow() = commandOrMessage.messageOrThrow()
}
