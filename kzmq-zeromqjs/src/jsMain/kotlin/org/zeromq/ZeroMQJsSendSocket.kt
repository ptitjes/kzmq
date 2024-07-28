/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.internal.zeromqjs.*

internal class ZeroMQJsSendSocket(private val underlying: Writable) : SendSocket {

    override suspend fun send(message: Message): Unit =
        underlying.send(message.frames.map { it.toBuffer() }.toTypedArray()).await()

    override suspend fun sendCatching(message: Message): SocketResult<Unit> = try {
        SocketResult.success(send(message))
    } catch (t: Throwable) {
        SocketResult.failure(t)
    }

    override fun trySend(message: Message): SocketResult<Unit> =
        throw NotImplementedError("trySend is not supported on JS target")

    override var multicastHops: Int by underlying::multicastHops
    override var sendBufferSize: Int by underlying::sendBufferSize
    override var sendHighWaterMark: Int by underlying::sendHighWaterMark
    override var sendTimeout: Int by underlying::sendTimeout
}
