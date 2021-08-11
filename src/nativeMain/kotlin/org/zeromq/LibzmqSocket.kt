package org.zeromq

import kotlinx.cinterop.*
import libzmq.*
import platform.posix.size_tVar

internal class LibzmqSocket internal constructor(private val underlying: COpaquePointer?) : Socket {

    override val type: Type
        get() = memScoped {
            val type = alloc<IntVar>()
            val size = alloc<size_tVar>()
            checkNativeError(zmq_getsockopt(underlying, ZMQ_TYPE, type.ptr, size.ptr))
            return Type.type(type.value)
        }

    override fun close() = checkNativeError(zmq_close(underlying))

    override fun bind(endpoint: String) = checkNativeError(zmq_bind(underlying, endpoint))

    override fun connect(endpoint: String) =
        checkNativeError(zmq_connect(underlying, endpoint))

    override fun disconnect(endpoint: String) =
        checkNativeError(zmq_disconnect(underlying, endpoint))

    override fun subscribe(topic: ByteArray) {
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

    override fun subscribe(topic: String) {
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

    override fun unsubscribe(topic: ByteArray) {
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

    override fun unsubscribe(topic: String) {
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

    override suspend fun send(data: ByteArray, sendMore: Boolean) {
        val nativeData = data.toCValues()
        val flags = if (sendMore) ZMQ_SNDMORE else 0
        checkNativeError(zmq_send(underlying, nativeData, nativeData.size.toULong(), flags))
    }

    private fun doReceive(flags: Int): ByteArray = memScoped {
        val msg = alloc<zmq_msg_t>()
        checkNativeError(zmq_msg_init(msg.ptr))
        checkNativeError(zmq_recvmsg(underlying, msg.ptr, flags))

        val data = zmq_msg_data(msg.ptr) ?: throw IllegalStateException("No message data")
        val size = zmq_msg_size(msg.ptr)
        val result = data.readBytes(size.toInt())

        checkNativeError(zmq_msg_close(msg.ptr))
        return result
    }

    override suspend fun receive(): ByteArray = doReceive(0)

    override fun receiveOrNull(): ByteArray? = try {
        doReceive(ZMQ_DONTWAIT)
    } catch (e: ZeroMQException) {
        if (e.error == ZeroMQError.EAGAIN) null else throw e
    }
}
