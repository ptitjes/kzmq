/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.channels.*
import org.zeromq.internal.*

internal interface CIOReceiveSocket : ReceiveSocket {

    val receiveChannel: ReceiveChannel<Message>
    val options: SocketOptions

    override suspend fun receive(): Message = receiveChannel.receive()

    override suspend fun receiveCatching(): SocketResult<Message> {
        val result = receiveChannel.receiveCatching()
        return if (result.isSuccess) SocketResult.success(result.getOrThrow())
        else SocketResult.failure(result.exceptionOrNull())
    }

    override fun tryReceive(): SocketResult<Message> {
        val result = receiveChannel.tryReceive()
        return if (result.isSuccess) SocketResult.success(result.getOrThrow())
        else SocketResult.failure(result.exceptionOrNull())
    }

    override val onReceive get() = receiveChannel.onReceive

    override operator fun iterator(): SocketIterator = object : SocketIterator {
        var nextMessage: Message? = null

        override suspend fun hasNext(): Boolean {
            if (nextMessage == null) nextMessage = receive()
            return nextMessage != null
        }

        override fun next(): Message {
            val message = nextMessage ?: error("No next message")
            nextMessage = null
            return message
        }
    }

    override var receiveBufferSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override var receiveHighWaterMark: Int
        get() = options.receiveQueueSize
        set(value) {
            options.receiveQueueSize = value
        }

    override var receiveTimeout: Int
        get() = TODO("Not yet implemented")
        set(value) {}
}
