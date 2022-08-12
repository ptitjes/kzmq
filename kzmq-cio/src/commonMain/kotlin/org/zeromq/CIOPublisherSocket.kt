/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*
import org.zeromq.internal.*

/**
 * An implementation of the [PUB socket](https://rfc.zeromq.org/spec/29/).
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
 * ## The PUB Socket Type
 *
 * The PUB socket type provides basic one-way broadcasting to a set of subscribers. Over TCP, it
 * does filtering on outgoing messages but nonetheless a message will be sent multiple times over
 * the network to reach multiple subscribers. PUB is used mainly for transient event distribution
 * where stability of the network (e.g. consistently low memory usage) is more important than
 * reliability of traffic.
 *
 * A. General behavior:
 * 1. MAY be connected to any number of SUB or XSUB subscribers, and SHALL only send messages.
 * 2. SHALL maintain a single outgoing message queue for each connected subscriber.
 * 3. SHALL create a queue when initiating an outgoing connection to a subscriber, and SHALL
 *    maintain the queue whether or not the connection is established.
 * 4. SHALL create a queue when a subscriber connects to it. If this subscriber disconnects, the
 *    PUB socket SHALL destroy its queue and SHALL discard any messages it contains.
 * 5. SHOULD constrain queue sizes to a runtime-configurable limit.
 * 6. SHALL silently discard any messages that subscribers send it.
 *
 * B. For processing outgoing messages:
 * 1. SHALL not modify outgoing messages in any way.
 * 2. MAY, depending on the transport, send all messages to all subscribers.
 * 3. MAY, depending on the transport, send messages only to subscribers who have a matching
 *    subscription.
 * 4. SHALL perform a binary comparison of the subscription against the start of the first frame of
 *    the message.
 * 5. SHALL silently drop the message if the queue for a subscriber is full.
 * 6. SHALL NOT block on sending.
 *
 * C. For processing subscriptions:
 * 1. SHALL receive subscribe and unsubscribe requests from subscribers depending on the transport
 *    protocol used.
 * 2. SHALL NOT deliver these commands to its calling application.
 */
internal class CIOPublisherSocket(
    engineInstance: CIOEngineInstance,
) : CIOSocket(engineInstance, Type.PUB), CIOSendSocket, PublisherSocket {

    override val validPeerTypes: Set<Type> get() = validPeerSocketTypes

    override val sendChannel = Channel<Message>()

    init {
        setHandler {
            val peerMailboxes = hashSetOf<PeerMailbox>()
            var subscriptions = SubscriptionTrie<PeerMailbox>()

            while (isActive) {
                select<Unit> {
                    peerEvents.onReceive { (kind, peerMailbox) ->
                        when (kind) {
                            PeerEvent.Kind.ADDITION -> peerMailboxes.add(peerMailbox)
                            PeerEvent.Kind.REMOVAL -> peerMailboxes.remove(peerMailbox)
                            else -> {}
                        }
                    }

                    for (peerMailbox in peerMailboxes) {
                        peerMailbox.receiveChannel.onReceive { commandOrMessage ->
                            logger.d { "Handling $commandOrMessage from $peerMailbox" }
                            subscriptions = when (val command = commandOrMessage.commandOrThrow()) {
                                is SubscribeCommand -> subscriptions.add(command.topic, peerMailbox)
                                is CancelCommand -> subscriptions.remove(command.topic, peerMailbox)
                                else -> protocolError("Expected SUBSCRIBE or CANCEL, but got ${command.name}")
                            }
                        }
                    }

                    sendChannel.onReceive { message ->
                        subscriptions.forEachMatching(message.first()) { peerMailbox ->
                            logger.d { "Dispatching $message to $peerMailbox" }
                            peerMailbox.sendChannel.send(CommandOrMessage(message))
                        }
                    }
                }
            }
        }
    }

    override var conflate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var invertMatching: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var noDrop: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    companion object {
        private val validPeerSocketTypes = setOf(Type.SUB, Type.XSUB)
    }
}
