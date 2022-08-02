/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.internal.*

internal abstract class CIOSocket(
    engineInstance: CIOEngineInstance,
    final override val type: Type,
) : Socket, SocketInfo, CoroutineScope {

    override val options = SocketOptions()

    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "An error occurred in socket" }
    }
    final override val coroutineContext =
        engineInstance.coroutineContext + job + exceptionHandler +
            CoroutineName("zmq-${type.toString().lowercase()}")

    override fun close() {
        job.cancel()
    }

    private val peerManager = PeerManager(
        coroutineContext,
        engineInstance.transportRegistry
    )
    protected val peerEvents = peerManager.peerEvents

    override suspend fun bind(endpoint: String) = peerManager.bind(endpoint, this)
    override suspend fun unbind(endpoint: String) = peerManager.unbind(endpoint)

    override fun connect(endpoint: String) = peerManager.connect(endpoint, this)
    override fun disconnect(endpoint: String) = peerManager.disconnect(endpoint)
}
