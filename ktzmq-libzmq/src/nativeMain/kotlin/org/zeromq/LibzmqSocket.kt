package org.zeromq

import kotlinx.cinterop.*
import kotlinx.coroutines.selects.*
import org.zeromq.internal.libzmq.*

internal abstract class LibzmqSocket internal constructor(
    private val underlying: COpaquePointer?,
    override val type: Type
) : Socket {

    override fun close() = checkNativeError(zmq_close(underlying))

    override fun bind(endpoint: String) =
        checkNativeError(zmq_bind(underlying, endpoint))

    override fun unbind(endpoint: String) =
        checkNativeError(zmq_unbind(underlying, endpoint))

    override fun connect(endpoint: String) =
        checkNativeError(zmq_connect(underlying, endpoint))

    override fun disconnect(endpoint: String) =
        checkNativeError(zmq_disconnect(underlying, endpoint))

    suspend fun subscribe() {
        subscribe(byteArrayOf().toCValues())
    }

    suspend fun subscribe(vararg topics: ByteArray) {
        if (topics.isEmpty()) subscribe(byteArrayOf().toCValues())
        else topics.forEach { subscribe(it.toCValues()) }
    }

    suspend fun subscribe(vararg topics: String) {
        if (topics.isEmpty()) subscribe("".cstr)
        else topics.forEach { subscribe(it.cstr) }
    }

    private fun subscribe(topic: CValues<ByteVar>) {
        checkNativeError(
            zmq_setsockopt(
                underlying,
                ZMQ_SUBSCRIBE,
                topic,
                topic.size.toULong()
            )
        )
    }

    suspend fun unsubscribe() {
        unsubscribe(byteArrayOf().toCValues())
    }

    suspend fun unsubscribe(vararg topics: ByteArray) {
        if (topics.isEmpty()) unsubscribe(byteArrayOf().toCValues())
        else topics.forEach { unsubscribe(it.toCValues()) }
    }

    suspend fun unsubscribe(vararg topics: String) {
        if (topics.isEmpty()) unsubscribe("".cstr)
        else topics.forEach { unsubscribe(it.cstr) }
    }

    private fun unsubscribe(topic: CValues<ByteVar>) {
        checkNativeError(
            zmq_setsockopt(
                underlying,
                ZMQ_UNSUBSCRIBE,
                topic,
                topic.size.toULong()
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

    var multicastHops: Int
            by socketOption(underlying, ZMQ_MULTICAST_HOPS, intConverter)

    var sendBufferSize: Int
            by socketOption(underlying, ZMQ_SNDBUF, intConverter)

    var sendHighWaterMark: Int
            by socketOption(underlying, ZMQ_SNDHWM, intConverter)

    var sendTimeout: Int
            by socketOption(underlying, ZMQ_SNDTIMEO, intConverter)

    suspend fun receive(): Message {
        TODO("Not yet implemented")
    }

    suspend fun receiveCatching(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    fun tryReceive(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    val onReceive: SelectClause1<Message> get() = TODO()

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

    var receiveBufferSize: Int
            by socketOption(underlying, ZMQ_RCVBUF, intConverter)

    var receiveHighWaterMark: Int
            by socketOption(underlying, ZMQ_RCVHWM, intConverter)

    var receiveTimeout: Int
            by socketOption(underlying, ZMQ_RCVTIMEO, intConverter)
}
