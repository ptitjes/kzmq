package org.zeromq

import org.zeromq.internal.zeromqjs.Subscriber as ZSubscriber

internal class ZeroMQJsSubscriberSocket internal constructor(override val underlying: ZSubscriber = ZSubscriber()) :
    ZeroMQJsSocket(),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    SubscriberSocket
{

    override fun subscribe(vararg topics: ByteArray) {
        underlying.subscribe(*topics.map { it.decodeToString() }.toTypedArray())
    }

    override fun subscribe(vararg topics: String) {
        underlying.subscribe(*topics)
    }

    override fun unsubscribe(vararg topics: ByteArray) {
        underlying.unsubscribe(*topics.map { it.decodeToString() }.toTypedArray())
    }

    override fun unsubscribe(vararg topics: String) {
        underlying.unsubscribe(*topics)
    }

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by underlying::invertMatching
}
