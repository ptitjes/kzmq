package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.*
import org.zeromq.internal.zeromqjs.*

class ZeroMQJsReceiveSocket(private val underlying: Readable) : ReceiveSocket {

    override suspend fun receive(): Message =
        Message(underlying.receive().await().map { it.toByteArray() })

    override suspend fun receiveCatching(): SocketResult<Message> = try {
        SocketResult.success(receive())
    } catch (t: Throwable) {
        SocketResult.failure(t)
    }

    override fun tryReceive(): SocketResult<Message> =
        throw NotImplementedError("Not supported on ZeroMQ.JS engine")

    override val onReceive: SelectClause1<Message>
        get() = throw NotImplementedError("Not supported on ZeroMQ.JS engine")

    override fun iterator(): SocketIterator {
        TODO("Not yet implemented")
    }

    override var receiveBufferSize: Int by underlying::receiveBufferSize
    override var receiveHighWaterMark: Int by underlying::receiveHighWaterMark
    override var receiveTimeout: Int by underlying::receiveTimeout
}
