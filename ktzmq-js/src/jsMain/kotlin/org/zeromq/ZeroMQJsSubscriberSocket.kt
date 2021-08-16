package org.zeromq

import kotlinx.coroutines.await
import zeromqjs.Subscriber as ZSubscriber

internal class ZeroMQJsSubscriberSocket internal constructor(override val underlying: ZSubscriber = ZSubscriber()) :
    ZeroMQJsSocket(), SubscriberSocket {

    override fun subscribe(topic: ByteArray): Unit {
        underlying.subscribe(topic.decodeToString())
    }

    override fun subscribe(topic: String): Unit {
        underlying.subscribe(topic)
    }

    override fun unsubscribe(topic: ByteArray): Unit {
        underlying.unsubscribe(topic.decodeToString())
    }

    override fun unsubscribe(topic: String): Unit {
        underlying.unsubscribe(topic)
    }

    override suspend fun receive(): Message =
        Message(underlying.receive().await().map { it.toByteArray() })

    override fun tryReceive(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    override fun iterator(): SocketIterator {
        TODO("Not yet implemented")
    }
}
