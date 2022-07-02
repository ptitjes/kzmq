/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
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

    protected abstract fun createPeerMessageHandler(): MessageHandler

    val socketOptions = SocketOptions()

    private val socketJob = Job()
    final override val coroutineContext = context + socketJob

    private val peerManager =
        PeerManager(
            coroutineContext,
            selectorManager,
            type,
            peerSocketTypes,
            socketOptions,
            ::createPeerMessageHandler,
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
