/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.internal.*

internal abstract class CIOSocket(
    private val engine: CIOEngine,
    final override val type: Type,
) : Socket, SocketInfo {

    override val options = SocketOptions()

    private val peerManager = PeerManager(
        engine.mainScope,
        engine.lingerScope,
        engine.transportRegistry
    )
    protected val peerEvents = peerManager.peerEvents

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "An error occurred in socket" }
    }
    private val coroutineName = CoroutineName("zmq-${type.toString().lowercase()}")

    private lateinit var socketJob: Job

    fun setHandler(block: suspend CoroutineScope.() -> Unit) {
        socketJob = engine.mainScope.launch(exceptionHandler + coroutineName) {
            block()
        }
    }

    override fun close() {
        peerEvents.cancel()
        socketJob.cancel()
        peerManager.close()
    }

    override suspend fun bind(endpoint: String) = peerManager.bind(endpoint, this)
    override suspend fun unbind(endpoint: String) = peerManager.unbind(endpoint)

    override fun connect(endpoint: String) = peerManager.connect(endpoint, this)
    override fun disconnect(endpoint: String) = peerManager.disconnect(endpoint)
}
