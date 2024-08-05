/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import io.kotest.core.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*

internal typealias SocketHandlerTest =
    suspend TestScope.(
        peerEvents: SendChannel<PeerEvent>,
        send: suspend (Message) -> Unit,
        receive: suspend () -> Message,
    ) -> Unit

internal suspend fun TestScope.withSocketHandler(
    handler: SocketHandler,
    block: SocketHandlerTest,
) = coroutineScope {
    val peerEvents = Channel<PeerEvent>()

    val handlerJob = launch { handler.handle(peerEvents) }

    block(peerEvents, handler::send, handler::receive)

    handlerJob.cancelAndJoin()
}
