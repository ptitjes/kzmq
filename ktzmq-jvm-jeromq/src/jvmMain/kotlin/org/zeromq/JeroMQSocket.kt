package org.zeromq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal abstract class JeroMQSocket internal constructor(private val underlying: ZMQ.Socket) :
    Socket {

    override fun close() = wrapException { underlying.close() }

    override suspend fun bind(endpoint: String): Unit =
        wrapException { underlying.bind(endpoint) }

    override fun connect(endpoint: String): Unit =
        wrapException { underlying.connect(endpoint) }

    override fun disconnect(endpoint: String): Unit =
        wrapException { underlying.disconnect(endpoint) }

    fun subscribe(topic: ByteArray): Unit =
        wrapException { underlying.subscribe(topic) }

    fun subscribe(topic: String): Unit =
        wrapException { underlying.subscribe(topic) }

    fun unsubscribe(topic: ByteArray): Unit =
        wrapException { underlying.unsubscribe(topic) }

    fun unsubscribe(topic: String): Unit =
        wrapException { underlying.unsubscribe(topic) }

    suspend fun send(message: Message): Unit =
        withContext(Dispatchers.IO) {
            wrapException {
                if (message.isSingle) {
                    underlying.send(message.singleOrThrow(), 0)
                } else {
                    val parts = message.parts
                    val partCount = parts.size
                    for ((index, part) in parts.withIndex()) {
                        underlying.send(part, if (index == partCount - 1) ZMQ.SNDMORE else 0)
                    }
                }
            }
        }

    fun trySend(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }

    suspend fun receive(): Message =
        withContext(Dispatchers.IO) {
            wrapException {
                val parts = mutableListOf<ByteArray>()
                do {
                    parts.add(underlying.recv())
                } while (underlying.hasReceiveMore())
                Message(*parts.toTypedArray())
            }
        }

    fun tryReceive(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    operator fun iterator(): SocketIterator = object : SocketIterator {
        var next: Message? = null

        override suspend fun hasNext(): Boolean {
            if (next == null) next = receive()
            return next != null
        }

        override fun next(): Message {
            val message = next ?: error("No next message")
            next = null
            return message
        }
    }
}
