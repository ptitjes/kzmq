/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.libzmq.*

@OptIn(ExperimentalForeignApi::class)
internal abstract class LibzmqSocket internal constructor(
    private val underlying: COpaquePointer?,
    override val type: Type,
) : Socket {

    override fun close() = checkNativeError(zmq_close(underlying))

    override suspend fun bind(endpoint: String) =
        checkNativeError(zmq_bind(underlying, endpoint))

    override suspend fun unbind(endpoint: String) =
        checkNativeError(zmq_unbind(underlying, endpoint))

    override fun connect(endpoint: String) =
        checkNativeError(zmq_connect(underlying, endpoint))

    override fun disconnect(endpoint: String) =
        checkNativeError(zmq_disconnect(underlying, endpoint))

    suspend fun subscribe() {
        subscribe(byteArrayOf().toCValues())
    }

    suspend fun subscribe(vararg topics: ByteString) {
        if (topics.isEmpty()) subscribe(byteArrayOf().toCValues())
        else topics.forEach { subscribe(it.toByteArray().toCValues()) }
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

    suspend fun unsubscribe(vararg topics: ByteString) {
        if (topics.isEmpty()) unsubscribe(byteArrayOf().toCValues())
        else topics.forEach { unsubscribe(it.toByteArray().toCValues()) }
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
        doSend(message, false)
    }

    suspend fun sendCatching(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }

    fun trySend(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }

    private fun doSend(message: Message, blocking: Boolean) {
        val frames = message.readFrames()
        val lastFrameIndex = frames.lastIndex

        val baseFlags = if (blocking) 0 else ZMQ_DONTWAIT

        for ((index, frame) in frames.withIndex()) {
            val nativeData = frame.readByteArray().toCValues()
            val flags = baseFlags or if (index < lastFrameIndex) ZMQ_SNDMORE else 0
            checkNativeError(zmq_send(underlying, nativeData, nativeData.size.toULong(), flags))
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

    suspend fun receive(): Message = coroutineScope {
        doReceiveMessage(false)
    }

    suspend fun receiveCatching(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    fun tryReceive(): SocketResult<Message> {
        TODO("Not yet implemented")
    }

    val onReceive: SelectClause1<Message> get() = TODO()

    private fun doReceiveMessage(blocking: Boolean): Message {
        val frames = mutableListOf<Buffer>()
        do {
            val frame = doReceiveMessagePart(blocking) ?: continue
            frames += Buffer().apply { write(frame) }
        } while (hasMoreParts)
        return Message(frames)
    }

    private val hasMoreParts: Boolean by socketOption(underlying, ZMQ_RCVMORE, booleanConverter)

    private fun doReceiveMessagePart(blocking: Boolean): ByteArray? = memScoped {
        val baseFlags = if (blocking) 0 else ZMQ_DONTWAIT
        val msg = alloc<zmq_msg_t>()
        checkNativeError(zmq_msg_init(msg.ptr))

        val errno = zmq_recvmsg(underlying, msg.ptr, baseFlags)
        if (errno == 11) return null
        checkNativeError(errno)

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
