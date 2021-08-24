package org.zeromq

import io.ktor.network.selector.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import org.zeromq.wire.*
import kotlin.coroutines.CoroutineContext

/**
 * An implementation of the [SUB socket](https://rfc.zeromq.org/spec/29/).
 *
 * ## The Publish-Subscribe Pattern
 *
 * The implementation SHOULD follow http://rfc.zeromq.org/spec:29/PUBSUB for the semantics of PUB,
 * XPUB, SUB and XSUB sockets.
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
    coroutineContext: CoroutineContext,
    selectorManager: SelectorManager
) : CIOSocket(coroutineContext, selectorManager, Type.SUB),
    CIOReceiveSocket,
    SubscriberSocket {

    private var subscriptions = mutableListOf<Subscription>()
    private var lateSubscriptionCommands = Channel<Command>(10)

    override val receiveChannel = Channel<Message>(1000)

    private val rawSockets = hashSetOf<RawSocket>()

    init {
        launch {
            while (isActive) {
                select<Unit> {
                    rawSocketActions.onReceive { (kind, rawSocket) ->
                        when (kind) {
                            RawSocketActionKind.ADDITION -> {
                                rawSockets.add(rawSocket)

                                for (subscription in subscriptions) {
                                    rawSocket.sendChannel.send(
                                        CommandOrMessage(SubscribeCommand(subscription.topic))
                                    )
                                }
                            }
                            RawSocketActionKind.REMOVAL -> {
                                rawSockets.remove(rawSocket)
                            }
                        }
                    }

                    lateSubscriptionCommands.onReceive { command ->
                        for (rawSocket in rawSockets) {
                            rawSocket.sendChannel.send(CommandOrMessage(command))
                        }
                    }

                    for (rawSocket in rawSockets) {
                        rawSocket.receiveChannel.onReceive { commandOrMessage ->
                            val message = commandOrMessage.messageOrThrow()
                            receiveChannel.send(message)
                        }
                    }
                }
            }
        }
    }

    override fun subscribe(vararg topics: ByteArray) {
        subscribe(topics.toList())
    }

    override fun subscribe(vararg topics: String) {
        subscribe(topics.map { it.encodeToByteArray() })
    }

    private fun subscribe(topics: List<ByteArray>) {
        for (topic in topics) {
            val subscription = Subscription(topic)
            subscriptions.add(subscription)
        }

        for (topic in topics) {
            lateSubscriptionCommands.trySend(SubscribeCommand(topic))
        }
    }

    override fun unsubscribe(vararg topics: ByteArray) {
        unsubscribe(topics.toList())
    }

    override fun unsubscribe(vararg topics: String) {
        unsubscribe(topics.map { it.encodeToByteArray() })
    }

    private fun unsubscribe(topics: List<ByteArray>) {
        val removedSubscriptions = mutableListOf<Subscription>()
        for (topic in topics) {
            val subscription = Subscription(topic)
            if (subscriptions.remove(subscription)) removedSubscriptions += subscription
        }

        for (topic in topics) {
            lateSubscriptionCommands.trySend(CancelCommand(topic))
        }
    }

    override var conflate: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var invertMatching: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
}
