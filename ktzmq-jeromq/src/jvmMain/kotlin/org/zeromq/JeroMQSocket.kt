package org.zeromq

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.withContext
import org.zeromq.internal.SelectInterest
import org.zeromq.internal.Selectable
import org.zeromq.internal.SelectorManager
import java.nio.channels.SelectableChannel

internal abstract class JeroMQSocket internal constructor(
    private val selector: SelectorManager,
    private val underlying: ZMQ.Socket,
    override val type: Type
) : Selectable(), Socket {

    override val socket: ZMQ.Socket
        get() = underlying

    override val channel: SelectableChannel
        get() = socket.base().fd ?: error("No file descriptor")

    override fun close() = wrappingExceptions { underlying.close() }

    override fun bind(endpoint: String): Unit =
        wrappingExceptions { underlying.bind(endpoint) }

    override fun unbind(endpoint: String): Unit =
        wrappingExceptions { underlying.unbind(endpoint) }

    override fun connect(endpoint: String): Unit =
        wrappingExceptions { underlying.connect(endpoint) }

    override fun disconnect(endpoint: String): Unit =
        wrappingExceptions { underlying.disconnect(endpoint) }

    suspend fun subscribe(): Unit = wrappingExceptions {
        underlying.subscribe(byteArrayOf())
    }

    suspend fun subscribe(vararg topics: ByteArray): Unit = wrappingExceptions {
        if (topics.isEmpty()) underlying.subscribe(byteArrayOf())
        else topics.forEach { underlying.subscribe(it) }
    }

    suspend fun subscribe(vararg topics: String): Unit = wrappingExceptions {
        if (topics.isEmpty()) underlying.subscribe("")
        else topics.forEach { underlying.subscribe(it) }
    }

    suspend fun unsubscribe(): Unit = wrappingExceptions {
        underlying.unsubscribe(byteArrayOf())
    }

    suspend fun unsubscribe(vararg topics: ByteArray): Unit = wrappingExceptions {
        if (topics.isEmpty()) underlying.unsubscribe(byteArrayOf())
        else topics.forEach { underlying.unsubscribe(it) }
    }

    suspend fun unsubscribe(vararg topics: String): Unit = wrappingExceptions {
        if (topics.isEmpty()) underlying.unsubscribe("")
        else topics.forEach { underlying.unsubscribe(it) }
    }

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
        selector.suspendForSelection(this, SelectInterest.WRITE)
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

    // TODO multicastHops is a long in underlying socket
    var multicastHops: Int by notImplementedProperty()
    var sendBufferSize: Int by underlying::sendBufferSize
    var sendHighWaterMark: Int by underlying::sndHWM
    var sendTimeout: Int by underlying::sendTimeOut

    suspend fun receive(): Message =
        wrappingExceptionsSuspend { suspendOnIO { receiveSuspend() } }

    suspend fun receiveCatching(): SocketResult<Message> =
        catchingExceptionsSuspend { suspendOnIO { receiveSuspend() } }

    fun tryReceive(): SocketResult<Message> =
        catchingExceptions { receiveImmediate() }

    val onReceive: SelectClause1<Message> get() =
        throw NotImplementedError("Not supported on JeroMQ engine")

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
        selector.suspendForSelection(this, SelectInterest.READ)
        trace("receivePartSuspend - receiving")
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

    var receiveBufferSize: Int by underlying::receiveBufferSize
    var receiveHighWaterMark: Int by underlying::rcvHWM
    var receiveTimeout: Int by underlying::receiveTimeOut

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
}

private suspend fun <T> suspendOnIO(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block)
