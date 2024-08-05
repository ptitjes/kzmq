/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.selects.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import zmq.*

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

    suspend fun subscribe(vararg topics: ByteString): Unit = wrapping {
        if (topics.isEmpty()) underlying.subscribe(byteArrayOf())
        else topics.forEach { underlying.subscribe(it.toByteArray()) }
    }

    suspend fun subscribe(vararg topics: String): Unit = wrapping {
        if (topics.isEmpty()) underlying.subscribe("")
        else topics.forEach { underlying.subscribe(it) }
    }

    suspend fun unsubscribe(): Unit = wrapping { underlying.unsubscribe(byteArrayOf()) }

    suspend fun unsubscribe(vararg topics: ByteString): Unit = wrapping {
        if (topics.isEmpty()) underlying.unsubscribe(byteArrayOf())
        else topics.forEach { underlying.unsubscribe(it.toByteArray()) }
    }

    suspend fun unsubscribe(vararg topics: String): Unit = wrapping {
        if (topics.isEmpty()) underlying.unsubscribe("")
        else topics.forEach { underlying.unsubscribe(it) }
    }

    suspend fun send(message: Message): Unit = wrapping { sendSuspend(message) }
    suspend fun sendCatching(message: Message): SocketResult<Unit> = catching { sendSuspend(message) }
    fun trySend(message: Message): SocketResult<Unit> = catching { sendImmediate(message) }

    private suspend fun sendSuspend(message: Message) = trace("sendSuspend") {
        val parts = message.readFrames()
        val lastIndex = parts.size - 1
        for ((index, part) in parts.withIndex()) {
            sendPartSuspend(part, index < lastIndex)
        }
    }

    private suspend fun sendPartSuspend(part: Buffer, sendMore: Boolean) {
        suspendOnIO { underlying.sendMsg(part.toMsg(), if (sendMore) ZMQ.SNDMORE else 0) }
    }

    private fun sendImmediate(message: Message) = trace("sendImmediate") {
        val parts = message.readFrames()
        val lastIndex = parts.size - 1
        for ((index, part) in parts.withIndex()) {
            sendPartImmediate(part, index < lastIndex)
        }
    }

    private fun sendPartImmediate(part: Buffer, sendMore: Boolean) {
        underlying.sendMsg(part.toMsg(), ZMQ.DONTWAIT or if (sendMore) ZMQ.SNDMORE else 0)
    }

    // TODO optimize?
    private fun Buffer.toMsg(): Msg = Msg.Builder().apply { put(readByteArray()) }.build()

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
        val parts = mutableListOf<Buffer>()
        do {
            parts.add(receivePartSuspend())
        } while (underlying.hasReceiveMore())
        return Message(parts)
    }

    private suspend fun receivePartSuspend(): Buffer {
        return suspendOnIO { underlying.recvMsg().toPart() }
    }

    private fun receiveImmediate(): Message = trace("receiveImmediate") {
        val parts = mutableListOf<Buffer>()
        do {
            parts.add(receivePartImmediate() ?: error("No message received"))
        } while (underlying.hasReceiveMore())
        return Message(parts)
    }

    private fun receivePartImmediate(): Buffer? {
        return underlying.recvMsg(ZMQ.DONTWAIT)?.toPart()
    }

    private fun Msg.toPart(): Buffer = Buffer().transferFrom(buf())

    var receiveBufferSize: Int by underlying::receiveBufferSize
    var receiveHighWaterMark: Int by underlying::rcvHWM
    var receiveTimeout: Int by underlying::receiveTimeOut
}
