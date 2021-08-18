package org.zeromq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal abstract class JeroMQSocket internal constructor(private val underlying: ZMQ.Socket) :
    Socket {

    override fun close() = wrappingExceptions { underlying.close() }

    override suspend fun bind(endpoint: String): Unit =
        wrappingExceptions { underlying.bind(endpoint) }

    override fun connect(endpoint: String): Unit =
        wrappingExceptions { underlying.connect(endpoint) }

    override fun disconnect(endpoint: String): Unit =
        wrappingExceptions { underlying.disconnect(endpoint) }

    fun subscribe(topic: ByteArray): Unit =
        wrappingExceptions { underlying.subscribe(topic) }

    fun subscribe(topic: String): Unit =
        wrappingExceptions { underlying.subscribe(topic) }

    fun unsubscribe(topic: ByteArray): Unit =
        wrappingExceptions { underlying.unsubscribe(topic) }

    fun unsubscribe(topic: String): Unit =
        wrappingExceptions { underlying.unsubscribe(topic) }

    suspend fun send(message: Message): Unit =
        withContext(Dispatchers.IO) { wrappingExceptions { doSend(message, true) } }

    suspend fun sendCatching(message: Message): SocketResult<Unit> =
        withContext(Dispatchers.IO) { catchingExceptions { doSend(message, true) } }

    fun trySend(message: Message): SocketResult<Unit> =
        catchingExceptions { doSend(message, false) }

    private fun doSend(message: Message, blocking: Boolean) {
        val flags = if (blocking) 0 else ZMQ.DONTWAIT
        if (message.isSingle) {
            underlying.send(message.singleOrThrow(), flags)
        } else {
            val parts = message.parts
            val partCount = parts.size
            for ((index, part) in parts.withIndex()) {
                underlying.send(part, flags or if (index == partCount - 1) ZMQ.SNDMORE else 0)
            }
        }
    }

    suspend fun receive(): Message =
        withContext(Dispatchers.IO) { wrappingExceptions { doReceive(true) } }

    suspend fun receiveCatching(): SocketResult<Message> =
        withContext(Dispatchers.IO) { catchingExceptions { doReceive(true) } }

    fun tryReceive(): SocketResult<Message> =
        catchingExceptions { doReceive(false) }

    private fun doReceive(blocking: Boolean): Message {
        val parts = mutableListOf<ByteArray>()
        val flags = if (blocking) 0 else ZMQ.DONTWAIT
        do {
            parts.add(underlying.recv(flags) ?: throw ZeroMQException(ZeroMQError.EAGAIN))
        } while (underlying.hasReceiveMore())
        return Message(*parts.toTypedArray())
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
