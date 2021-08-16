package org.zeromq

import kotlinx.coroutines.await
import zeromqjs.Publisher as ZPublisher

internal class ZeroMQJsPublisherSocket internal constructor(override val underlying: ZPublisher = ZPublisher()) :
    ZeroMQJsSocket(), PublisherSocket {

    override suspend fun send(message: Message): Unit =
        underlying.send(message.parts.map { it.toBuffer() }.toTypedArray()).await()

    override fun trySend(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }
}
