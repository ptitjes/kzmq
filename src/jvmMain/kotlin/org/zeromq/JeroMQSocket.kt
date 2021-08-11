package org.zeromq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class JeroMQSocket internal constructor(private val underlying: ZMQ.Socket) : Socket {

    override val type: Type
        get() = Type.type(underlying.type)

    override fun close() = wrapException { underlying.close() }

    override fun bind(endpoint: String): Unit = wrapException { underlying.bind(endpoint) }

    override fun connect(endpoint: String): Unit = wrapException { underlying.connect(endpoint) }
    override fun disconnect(endpoint: String): Unit =
        wrapException { underlying.disconnect(endpoint) }

    override fun subscribe(topic: ByteArray): Unit = wrapException { underlying.subscribe(topic) }
    override fun subscribe(topic: String): Unit = wrapException { underlying.subscribe(topic) }
    override fun unsubscribe(topic: ByteArray): Unit =
        wrapException { underlying.unsubscribe(topic) }

    override fun unsubscribe(topic: String): Unit = wrapException { underlying.unsubscribe(topic) }

    override suspend fun send(data: ByteArray, sendMore: Boolean): Unit =
        withContext(Dispatchers.IO) {
            wrapException {
                underlying.send(data, if (sendMore) ZMQ.SNDMORE else 0)
            }
        }

    override suspend fun receive(): ByteArray =
        withContext(Dispatchers.IO) {
            wrapException {
                underlying.recv()
            }
        }

    override fun receiveOrNull(): ByteArray? = wrapException { underlying.recv(ZMQ.DONTWAIT) }
}
