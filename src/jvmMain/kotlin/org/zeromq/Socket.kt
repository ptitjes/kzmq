package org.zeromq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SocketImpl internal constructor(private val underlying: ZMQ.Socket) : Socket {

    override val type: Type
        get() = Type.type(underlying.type)

    override fun close() = underlying.close()

    override fun bind(endpoint: String) = underlying.bind(endpoint)

    override fun connect(endpoint: String) = underlying.connect(endpoint)
    override fun disconnect(endpoint: String) = underlying.disconnect(endpoint)

    override fun subscribe(topic: ByteArray) = underlying.subscribe(topic)
    override fun subscribe(topic: String) = underlying.subscribe(topic)
    override fun unsubscribe(topic: ByteArray) = underlying.unsubscribe(topic)
    override fun unsubscribe(topic: String) = underlying.unsubscribe(topic)

    override suspend fun send(data: ByteArray, sendMore: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            underlying.send(data, if (sendMore) ZMQ.SNDMORE else 0)
        }

    override suspend fun receive(): ByteArray =
        withContext(Dispatchers.IO) {
            underlying.recv()
        }

    override fun receiveOrNull(): ByteArray? = underlying.recv(ZMQ.DONTWAIT)
}
