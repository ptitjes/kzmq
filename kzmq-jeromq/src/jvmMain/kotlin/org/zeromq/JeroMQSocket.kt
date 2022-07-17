/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.selects.*

@Suppress("RedundantSuspendModifier")
internal abstract class JeroMQSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
    underlyingType: SocketType,
    override val type: Type,
) : Socket {

    protected val underlying = factory(underlyingType)

    override fun close() = wrapping { underlying.close() }

    override suspend fun bind(endpoint: String): Unit = wrapping { underlying.bind(endpoint) }
    override suspend fun unbind(endpoint: String): Unit = wrapping { underlying.unbind(endpoint) }
    override fun connect(endpoint: String): Unit = wrapping { underlying.connect(endpoint) }
    override fun disconnect(endpoint: String): Unit = wrapping { underlying.disconnect(endpoint) }

    suspend fun subscribe(): Unit = wrapping { underlying.subscribe(byteArrayOf()) }

    suspend fun subscribe(vararg topics: ByteArray): Unit = wrapping {
        if (topics.isEmpty()) underlying.subscribe(byteArrayOf())
        else topics.forEach { underlying.subscribe(it) }
    }

    suspend fun subscribe(vararg topics: String): Unit = wrapping {
        if (topics.isEmpty()) underlying.subscribe("")
        else topics.forEach { underlying.subscribe(it) }
    }

    suspend fun unsubscribe(): Unit = wrapping { underlying.unsubscribe(byteArrayOf()) }

    suspend fun unsubscribe(vararg topics: ByteArray): Unit = wrapping {
        if (topics.isEmpty()) underlying.unsubscribe(byteArrayOf())
        else topics.forEach { underlying.unsubscribe(it) }
    }

    suspend fun unsubscribe(vararg topics: String): Unit = wrapping {
        if (topics.isEmpty()) underlying.unsubscribe("")
        else topics.forEach { underlying.unsubscribe(it) }
    }

    suspend fun send(message: Message): Unit = wrapping { sendSuspend(message) }
    suspend fun sendCatching(message: Message): SocketResult<Unit> = catching { sendSuspend(message) }
    fun trySend(message: Message): SocketResult<Unit> = catching { sendImmediate(message) }

    private suspend fun sendSuspend(message: Message) = trace("sendSuspend") {
        do {
            sendFrameSuspend(message.removeFirst(), message.isNotEmpty())
        } while (message.isNotEmpty())
    }

    private suspend fun sendFrameSuspend(frame: Frame, sendMore: Boolean) {
        suspendOnIO {
            frame.read { array, offset, length ->
                underlying.send(array, offset, length, if (sendMore) ZMQ.SNDMORE else 0)
            }
            frame.release()
        }
    }

    private fun sendImmediate(message: Message) = trace("sendImmediate") {
        do {
            sendFrameImmediate(message.removeFirst(), message.isNotEmpty())
        } while (message.isNotEmpty())
    }

    private fun sendFrameImmediate(frame: Frame, sendMore: Boolean) {
        frame.read { array, offset, length ->
            underlying.send(array, offset, length, ZMQ.DONTWAIT or if (sendMore) ZMQ.SNDMORE else 0)
        }
        frame.release()
    }

    // TODO multicastHops is a long in underlying socket
    var multicastHops: Int by notImplementedProperty()
    var sendBufferSize: Int by underlying::sendBufferSize
    var sendHighWaterMark: Int by underlying::sndHWM
    var sendTimeout: Int by underlying::sendTimeOut

    suspend fun receive(): Message = wrapping { receiveSuspend() }
    suspend fun receiveCatching(): SocketResult<Message> = catching { receiveSuspend() }
    fun tryReceive(): SocketResult<Message> = catching { receiveImmediate() }

    val onReceive: SelectClause1<Message>
        get() = throw NotImplementedError("Not supported on JeroMQ engine")

    private suspend fun receiveSuspend(): Message = trace("receiveSuspend") {
        val frames = mutableListOf<Frame>()
        do {
            frames.add(receiveFrameSuspend())
        } while (underlying.hasReceiveMore())
        return messageOf(*frames.toTypedArray())
    }

    private suspend fun receiveFrameSuspend(): Frame {
        return suspendOnIO { constantFrameOf(underlying.recv(0)) }
    }

    private fun receiveImmediate(): Message = trace("receiveImmediate") {
        val frames = mutableListOf<Frame>()
        do {
            frames.add(receiveFrameImmediate() ?: error("No message received"))
        } while (underlying.hasReceiveMore())
        return messageOf(*frames.toTypedArray())
    }

    private fun receiveFrameImmediate(): Frame? {
        return constantFrameOf(underlying.recv(ZMQ.DONTWAIT))
    }

    operator fun iterator(): SocketIterator = object : SocketIterator {
        var next: Message? = null

        override suspend fun hasNext(): Boolean {
            if (next == null) next = receive()
            // TODO fix the fact that we should return false when the socket is closed
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
}
