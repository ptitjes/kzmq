package org.zeromq

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal abstract class JeroMQSocket internal constructor(
    private val context: JeroMQInstance,
    private val underlying: ZMQ.Socket
) : Socket {

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
        wrappingExceptionsSuspend { suspendOnIO { sendSuspend(message) } }

    suspend fun sendCatching(message: Message): SocketResult<Unit> =
        catchingExceptionsSuspend { suspendOnIO { sendSuspend(message) } }

    fun trySend(message: Message): SocketResult<Unit> =
        catchingExceptions { sendImmediate(message) }

    private suspend fun sendSuspend(message: Message) = traceSuspending("sendSuspend") {
        val parts = message.parts
        val lastIndex = parts.size - 1
        for ((index, part) in parts.withIndex()) {
            sendPartSuspend(part, index < lastIndex)
        }
    }

    private suspend fun sendPartSuspend(part: ByteArray, sendMore: Boolean) {
        trace("sendPartSuspend - fast path")
        try {
            sendPartImmediate(part, sendMore)
            return
        } catch (t: Throwable) {
        }

        trace("sendPartSuspend - slow path")
        context.suspendUntilEvents(underlying, ZPoller.OUT)
        trace("sendPartSuspend - sending")
        sendPartImmediate(part, sendMore)
    }

    private fun sendImmediate(message: Message) = trace("sendImmediate") {
        val parts = message.parts
        val lastIndex = parts.size - 1
        for ((index, part) in parts.withIndex()) {
            sendPartImmediate(part, index < lastIndex)
        }
    }

    private fun sendPartImmediate(part: ByteArray, sendMore: Boolean) =
        underlying.send(part, ZMQ.DONTWAIT or if (sendMore) ZMQ.SNDMORE else 0)

    suspend fun receive(): Message =
        wrappingExceptionsSuspend { suspendOnIO { receiveSuspend() } }

    suspend fun receiveCatching(): SocketResult<Message> =
        catchingExceptionsSuspend { suspendOnIO { receiveSuspend() } }

    fun tryReceive(): SocketResult<Message> =
        catchingExceptions { receiveImmediate() }

    private suspend fun receiveSuspend(): Message = traceSuspending("receiveSuspend") {
        val parts = mutableListOf<ByteArray>()
        do {
            parts.add(receivePartSuspend())
        } while (underlying.hasReceiveMore())
        return@traceSuspending Message(*parts.toTypedArray())
    }

    private suspend fun receivePartSuspend(): ByteArray {
        trace("receivePartSuspend - fast path")
        val part = receivePartImmediate()
        if (part != null) return part

        trace("receivePartSuspend - slow path")
        context.suspendUntilEvents(underlying, ZPoller.IN)
        trace("receivePartSuspend - sending")
        return receivePartImmediate() ?: error("Invalid state")
    }

    private fun receiveImmediate(): Message = trace("receiveImmediate") {
        val parts = mutableListOf<ByteArray>()
        do {
            parts.add(receivePartImmediate() ?: error("No message received"))
        } while (underlying.hasReceiveMore())
        return@trace Message(*parts.toTypedArray())
    }

    private fun receivePartImmediate(): ByteArray? = underlying.recv(ZMQ.DONTWAIT)

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

    private suspend fun <T> traceSuspending(function: String, block: suspend () -> T): T = try {
        trace("$function - before")
        block()
    } finally {
        trace("$function - after")
    }

    private fun <T> trace(function: String, block: () -> T): T = try {
        trace("$function - before")
        block()
    } finally {
        trace("$function - after")
    }

    private fun trace(message: String) {
        if (TRACE) println("$underlying: $message")
    }
}

private suspend fun <T> suspendOnIO(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block)
