/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.inproc

import io.ktor.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.zeromq.internal.*
import org.zeromq.internal.PeerEvent.Kind.*
import kotlin.coroutines.*
import kotlin.random.*

internal class InprocTransport(
    private val coroutineContext: CoroutineContext,
) : Transport {
    private val handlers = atomic(mapOf<String, InprocEndpointHandler>())

    override fun supportsSchemes(scheme: String) = scheme == "inproc"
    override val isMulticast get() = false

    override fun close() {
        val handlers = handlers.getAndSet(mapOf())
        handlers.forEach { (_, handler) -> handler.close() }
    }

    private fun getOrCreateHandlerFor(endpoint: String): InprocEndpointHandler {
        val name = parseInprocEndpoint(endpoint).name
        return handlers.updateAndGet {
            if (it[name] == null) {
                it + (name to InprocEndpointHandler(coroutineContext, endpoint))
            } else it
        }[name] ?: error("Failed to get handler for endpoint: $endpoint")
    }

    override suspend fun bind(
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        endpoint: String,
    ) = coroutineScope {
        val handler = getOrCreateHandlerFor(endpoint)
        try {
            handler.acquire()
            handler.notify(InprocEndpointEvent.Binding(peerManager) {
                val randomId = Random.Default.nextBytes(8).encodeBase64()
                PeerMailbox("$endpoint/$randomId", socketInfo.options)
            })

            awaitCancellation()
        } finally {
            handler.notify(InprocEndpointEvent.Unbinding)
            handler.release()
        }
    }

    override suspend fun connect(
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        endpoint: String,
    ) = coroutineScope {
        val mailbox = PeerMailbox(endpoint, socketInfo.options)

        val handler = getOrCreateHandlerFor(endpoint)
        try {
            handler.acquire()
            peerManager.notify(PeerEvent(ADDITION, mailbox))
            handler.notify(InprocEndpointEvent.Connecting(mailbox, peerManager))

            awaitCancellation()
        } finally {
            handler.notify(InprocEndpointEvent.Disconnecting(mailbox))
            peerManager.notify(PeerEvent(REMOVAL, mailbox))
            handler.release()
        }
    }
}
