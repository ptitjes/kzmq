/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import de.infix.testBalloon.framework.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*

internal typealias SocketHandlerTest =
    suspend TestExecutionScope.(
        peerEvents: SendChannel<PeerEvent>,
        send: suspend (Message) -> Unit,
        receive: suspend () -> Message,
    ) -> Unit

internal suspend fun TestExecutionScope.withSocketHandler(
    handler: SocketHandler,
    block: SocketHandlerTest,
) = coroutineScope {
    val peerEvents = Channel<PeerEvent>()

    val handlerJob = launch { handler.handle(peerEvents) }

    block(peerEvents, handler::send, handler::receive)

    handlerJob.cancelAndJoin()
}
