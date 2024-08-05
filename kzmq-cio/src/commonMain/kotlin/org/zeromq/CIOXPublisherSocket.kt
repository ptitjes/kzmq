/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*
import kotlinx.io.*
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
 * ## The XPUB Socket Type
 *
 * The XPUB socket type extends the PUB socket with the ability to receive messages from anonymous
 * subscribers, and the exposure of subscription commands to the application. XPUB is usually used
 * in proxies but is also useful for advanced applications.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of SUB or XSUB subscribers, and MAY both send and receive
 *    messages.
 * 2. SHALL maintain a double queue for each connected subscriber, allowing outgoing and incoming
 *    messages to be queued independently.
 * 3. SHALL create a double queue when initiating an outgoing connection to a subscriber, and
 *    SHALL maintain the double queue whether or not the connection is established.
 * 4. SHALL create a double queue when a subscriber connects to it. If this subscriber disconnects,
 *    the XPUB socket SHALL destroy its double queue and SHALL discard any messages it contains.
 * 5. SHOULD constrain incoming and outgoing queue sizes to a runtime-configurable limit.
 *
 * B. For processing outgoing messages:
 * 1. SHALL not modify outgoing messages in any way.
 * 2. MAY, depending on the transport, send all messages to all subscribers.
 * 3. MAY, depending on the transport, send messages only to subscribers who have a matching
 *    subscription.
 * 4. SHALL perform a binary comparison of the subscription against the start of the first frame
 *    of the message.
 * 5. SHALL silently drop the message if the queue for a subscriber is full.
 * 6. SHALL NOT block on sending.
 *
 * C. For processing incoming messages:
 * 1. SHALL receive incoming messages from its subscribers using a fair-queuing strategy.
 * 2. SHALL deliver these messages to its calling application.
 *
 * D. For processing subscriptions:
 * 1. SHALL receive subscribe and unsubscribe requests from subscribers depending on the transport
 *    protocol used.
 * 2. SHALL deliver these commands to its calling application.
 * 3. MAY, depending on configuration, normalize commands delivered to its calling application so
 *    that multiple identical subscriptions result in a single command only.
 * 4. SHALL, if the subscriber peer disconnects prematurely, generate a suitable unsubscribe
 *    request for the calling application.
 */
internal class CIOXPublisherSocket(
    engine: CIOEngine,
) : CIOSocket(engine, Type.XPUB), CIOSendSocket, CIOReceiveSocket, XPublisherSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes
    override val handler = setupHandler(XPublisherSocketHandler())

    override var invertMatching: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var noDrop: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var manual: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var welcomeMessage: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    companion object {
        private val validPeerSocketTypes = setOf(Type.SUB, Type.XSUB)
    }
}

internal class XPublisherSocketHandler : SocketHandler {
    private val mailboxes = hashSetOf<PeerMailbox>()
    private var subscriptions = SubscriptionTrie<PeerMailbox>()

    override suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>) = coroutineScope {
        while (isActive) {
            select<Unit> {
                peerEvents.onReceive { (kind, peerMailbox) ->
                    when (kind) {
                        PeerEvent.Kind.ADDITION -> mailboxes.add(peerMailbox)
                        PeerEvent.Kind.REMOVAL -> mailboxes.remove(peerMailbox)
                        else -> {}
                    }
                }
            }
        }
    }

    override suspend fun send(message: Message) {
        subscriptions.forEachMatching(message.peekFirstFrame().readByteArray()) { peerMailbox ->
            logger.d { "Dispatching $message to $peerMailbox" }
            peerMailbox.sendChannel.send(CommandOrMessage(message))
        }
    }

    override suspend fun receive(): Message {
        return select {
            for (mailbox in mailboxes) {
                mailbox.receiveChannel.onReceive { commandOrMessage ->
                    logger.d { "Handling $commandOrMessage from $mailbox" }
                    if (commandOrMessage.isCommand) {
                        when (val command = commandOrMessage.commandOrThrow()) {
                            is SubscribeCommand -> {
                                subscriptions = subscriptions.add(command.topic, mailbox)
                                SubscriptionMessage(true, command.topic).toMessage()
                            }

                            is CancelCommand -> {
                                subscriptions = subscriptions.remove(command.topic, mailbox)
                                SubscriptionMessage(false, command.topic).toMessage()
                            }

                            else -> protocolError("Expected SUBSCRIBE or CANCEL, but got ${command.name}")
                        }
                    } else {
                        commandOrMessage.messageOrThrow()
                    }
                }
            }
        }
    }
}
