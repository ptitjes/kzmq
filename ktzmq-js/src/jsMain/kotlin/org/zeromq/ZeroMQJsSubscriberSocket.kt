package org.zeromq

import kotlinx.coroutines.await
import org.zeromq.internal.zeromqjs.Subscriber as ZSubscriber

internal class ZeroMQJsSubscriberSocket internal constructor(override val underlying: ZSubscriber = ZSubscriber()) :
    ZeroMQJsSocket(), SubscriberSocket {

    override fun subscribe(topic: ByteArray) {
        underlying.subscribe(topic.decodeToString())
    }

    override fun subscribe(topic: String) {
        underlying.subscribe(topic)
    }

    override fun unsubscribe(topic: ByteArray) {
        underlying.unsubscribe(topic.decodeToString())
    }

    override fun unsubscribe(topic: String) {
        underlying.unsubscribe(topic)
    }

    override suspend fun receive(): Message =
        Message(underlying.receive().await().map { it.toByteArray() })

    override suspend fun receiveCatching(): SocketResult<Message> = try {
        SocketResult.success(receive())
    } catch (t: Throwable) {
        SocketResult.failure(t)
    }

    override fun tryReceive(): SocketResult<Message> =
        throw NotImplementedError("tryReceive is not supported on JS target")

    override fun iterator(): SocketIterator {
        TODO("Not yet implemented")
    }
}
