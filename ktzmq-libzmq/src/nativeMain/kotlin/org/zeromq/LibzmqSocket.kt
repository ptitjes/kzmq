package org.zeromq

import kotlinx.cinterop.*
import org.zeromq.internal.libzmq.*

internal abstract class LibzmqSocket internal constructor(private val underlying: COpaquePointer?) :
    Socket {

    override fun close() = checkNativeError(zmq_close(underlying))

    override suspend fun bind(endpoint: String) =
        checkNativeError(zmq_bind(underlying, endpoint))

    override fun connect(endpoint: String) =
        checkNativeError(zmq_connect(underlying, endpoint))

    override fun disconnect(endpoint: String) =
        checkNativeError(zmq_disconnect(underlying, endpoint))

    fun subscribe(topic: ByteArray) {
        val topicCString = topic.toCValues()
        checkNativeError(
            zmq_setsockopt(
                underlying,
                ZMQ_SUBSCRIBE,
                topicCString,
                topicCString.size.toULong()
            )
        )
    }

    fun subscribe(topic: String) {
        val topicCString = topic.cstr
        checkNativeError(
            zmq_setsockopt(
                underlying,
                ZMQ_SUBSCRIBE,
                topicCString,
                topicCString.size.toULong()
            )
        )
    }

    fun unsubscribe(topic: ByteArray) {
        val topicCString = topic.toCValues()
        checkNativeError(
            zmq_setsockopt(
                underlying,
                ZMQ_UNSUBSCRIBE,
                topicCString,
                topicCString.size.toULong()
            )
        )
    }

    fun unsubscribe(topic: String) {
        val topicCString = topic.cstr
        checkNativeError(
            zmq_setsockopt(
                underlying,
                ZMQ_UNSUBSCRIBE,
                topicCString,
                topicCString.size.toULong()
            )
        )
    }

    suspend fun send(message: Message) {
        doSend(message, true)
    }

    suspend fun sendCatching(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }

    fun trySend(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }

    private fun doSend(message: Message, blocking: Boolean) {
        val baseFlags = if (blocking) 0 else ZMQ_DONTWAIT
        if (message.isSingle) {
            val nativeData = message.singleOrThrow().toCValues()
            checkNativeError(zmq_send(underlying, nativeData, nativeData.size.toULong(), baseFlags))
        } else {
            val parts = message.parts
            val partCount = parts.size
            for ((index, part) in parts.withIndex()) {
                val nativeData = part.toCValues()
                val flags = baseFlags or if (index == partCount - 1) ZMQ_SNDMORE else 0
                checkNativeError(zmq_send(underlying, nativeData, nativeData.size.toULong(), flags))
            }
        }
    }

    suspend fun receive(): Message {
        TODO("Not yet implemented")
    }

    suspend fun receiveCatching(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    fun tryReceive(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    fun iterator(): SocketIterator {
        TODO("Not yet implemented")
    }

    private fun doReceive(blocking: Boolean): ByteArray = memScoped {
        val baseFlags = if (blocking) 0 else ZMQ_DONTWAIT
        val msg = alloc<zmq_msg_t>()
        checkNativeError(zmq_msg_init(msg.ptr))
        checkNativeError(zmq_recvmsg(underlying, msg.ptr, baseFlags))

        val data = zmq_msg_data(msg.ptr) ?: throw IllegalStateException("No message data")
        val size = zmq_msg_size(msg.ptr)
        val result = data.readBytes(size.toInt())

        checkNativeError(zmq_msg_close(msg.ptr))
        return result
    }
}
