/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.*

internal interface CIOReceiveSocket : ReceiveSocket {

    val handler: SocketHandler
    val options: SocketOptions

    override suspend fun receive(): Message = handler.receive()

    override suspend fun receiveCatching(): SocketResult<Message> {
        val result = runCatching { receive() }
        return if (result.isSuccess) SocketResult.success(result.getOrThrow())
        else SocketResult.failure(result.exceptionOrNull())
    }

    override fun tryReceive(): SocketResult<Message> {
        val maybeMessage = handler.tryReceive()
        return if (maybeMessage != null) SocketResult.success(maybeMessage)
        else SocketResult.failure()
    }

    override val onReceive get() = TODO()

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
