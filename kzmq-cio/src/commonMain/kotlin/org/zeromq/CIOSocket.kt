/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.ktor.network.selector.*
import kotlinx.coroutines.*
import org.zeromq.internal.*
import kotlin.coroutines.*

internal abstract class CIOSocket(
    context: CoroutineContext,
    selectorManager: SelectorManager,
    final override val type: Type,
    peerSocketTypes: Set<Type>,
) : Socket, CoroutineScope {

    val socketOptions = SocketOptions()

    private val socketJob = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "An error occurred while managing peers" }
    }
    final override val coroutineContext = context + socketJob + exceptionHandler

    private val peerManager =
        PeerManager(
            coroutineContext,
            selectorManager,
            type,
            peerSocketTypes,
            socketOptions,
        )
    protected val peerEvents = peerManager.peerEvents

    override fun close() {
        socketJob.cancel()
    }

    override suspend fun bind(endpoint: String) = peerManager.bind(endpoint)
    override suspend fun unbind(endpoint: String) = peerManager.unbind(endpoint)
    override fun connect(endpoint: String) = peerManager.connect(endpoint)
    override fun disconnect(endpoint: String) = peerManager.disconnect(endpoint)
}
