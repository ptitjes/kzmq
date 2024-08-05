/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.channels.*
import org.zeromq.internal.*

internal interface CIOSendSocket : SendSocket {

    val handler: SocketHandler
    val options: SocketOptions

    override suspend fun send(message: Message) = handler.send(message)

    override suspend fun sendCatching(message: Message): SocketResult<Unit> {
        val result = runCatching { send(message) }
        return if (result.isSuccess) SocketResult.success(result.getOrThrow())
        else SocketResult.failure(result.exceptionOrNull())
    }

    override fun trySend(message: Message): SocketResult<Unit> {
        TODO()
    }

    override var multicastHops: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override var sendBufferSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override var sendHighWaterMark: Int
        get() = options.sendQueueSize
        set(value) {
            options.sendQueueSize = value
        }

    override var sendTimeout: Int
        get() = TODO("Not yet implemented")
        set(value) {}
}
