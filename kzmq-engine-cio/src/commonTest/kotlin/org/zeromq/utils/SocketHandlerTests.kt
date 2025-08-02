/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*

internal suspend fun <H : SocketHandler> ((options: SocketOptions) -> H).runTest(
    test: suspend SocketHandlerTestScope<H>.() -> Unit,
) = coroutineScope {
    val options = SocketOptions()
    val handler: H = this@runTest(options)
    val peerEvents = Channel<PeerEvent>()
    val handlerJob = launch { handler.handle(peerEvents) }

    val scope = object : SocketHandlerTestScope<H> {
        override val socketOptions = options
        override val handler = handler
        override val peerEvents = peerEvents

        override suspend fun send(message: Message) = handler.send(message)
        override fun trySend(message: Message) = handler.trySend(message)
        override suspend fun receive(): Message = handler.receive()
        override fun tryReceive(): Message? = handler.tryReceive()
    }

    scope.test()
    handlerJob.cancelAndJoin()
}

internal interface SocketHandlerTestScope<H : SocketHandler> {
    val socketOptions: SocketOptions
    val handler: H
    val peerEvents: SendChannel<PeerEvent>

    suspend fun send(message: Message): Unit
    fun trySend(message: Message): Unit?
    suspend fun receive(): Message
    fun tryReceive(): Message?
}
