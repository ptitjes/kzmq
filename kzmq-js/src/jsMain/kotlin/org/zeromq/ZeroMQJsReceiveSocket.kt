/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.*
import org.zeromq.internal.zeromqjs.*

internal class ZeroMQJsReceiveSocket(private val underlying: Readable) : ReceiveSocket {

    override suspend fun receive(): Message =
        messageOf(underlying.receive().await().map { constantFrameOf(it.asByteArray()) })

    override suspend fun receiveCatching(): SocketResult<Message> = try {
        SocketResult.success(receive())
    } catch (t: Throwable) {
        SocketResult.failure(t)
    }

    override fun tryReceive(): SocketResult<Message> =
        throw NotImplementedError("Not supported on ZeroMQ.JS engine")

    override val onReceive: SelectClause1<Message>
        get() = throw NotImplementedError("Not supported on ZeroMQ.JS engine")

    override fun iterator(): SocketIterator {
        TODO("Not yet implemented")
    }

    override var receiveBufferSize: Int by underlying::receiveBufferSize
    override var receiveHighWaterMark: Int by underlying::receiveHighWaterMark
    override var receiveTimeout: Int by underlying::receiveTimeout
}
