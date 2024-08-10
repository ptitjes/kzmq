/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import io.kotest.core.spec.style.*
import io.kotest.core.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*

internal abstract class SocketHandlerTests(
    private val factory: () -> SocketHandler,
    body: SocketHandlerTests.() -> Unit,
) : FunSpec() {
    init {
        body()
    }

    suspend fun TestScope.withHandler(
        test: suspend TestScope.(
            peerEvents: SendChannel<PeerEvent>,
            send: suspend (Message) -> Unit,
            receive: suspend () -> Message
        ) -> Unit
    ) = coroutineScope {
        val handler = factory()
        val peerEvents = Channel<PeerEvent>()
        val handlerJob = launch { handler.handle(peerEvents) }
        test(peerEvents, handler::send, handler::receive)
        handlerJob.cancelAndJoin()
    }
}
