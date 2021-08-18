package org.zeromq

import kotlinx.coroutines.await
import org.zeromq.internal.zeromqjs.Publisher as ZPublisher

internal class ZeroMQJsPublisherSocket internal constructor(override val underlying: ZPublisher = ZPublisher()) :
    ZeroMQJsSocket(), PublisherSocket {

    override suspend fun send(message: Message): Unit =
        underlying.send(message.parts.map { it.toBuffer() }.toTypedArray()).await()

    override suspend fun sendCatching(message: Message): SocketResult<Unit> = try {
        SocketResult.success(send(message))
    } catch (t: Throwable) {
        SocketResult.failure(t)
    }

    override fun trySend(message: Message): SocketResult<Unit> =
        throw NotImplementedError("trySend is not supported on JS target")
}
