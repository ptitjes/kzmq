/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.inproc

import io.ktor.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.zeromq.internal.*
import org.zeromq.internal.Identity
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

    override fun bind(
        mainScope: CoroutineScope,
        lingerScope: CoroutineScope,
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        address: String,
    ): BindingHolder = InprocBindingHolder(
        this::getOrCreateHandlerFor,
        mainScope,
        peerManager,
        socketInfo,
        address
    )

    override fun connect(
        mainScope: CoroutineScope,
        lingerScope: CoroutineScope,
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        address: String,
    ): ConnectionHolder = InprocConnectionHolder(
        this::getOrCreateHandlerFor,
        mainScope,
        lingerScope,
        peerManager,
        socketInfo,
        address
    )
}

internal class InprocBindingHolder(
    handlerFactory: (endpoint: String) -> InprocEndpointHandler,
    mainScope: CoroutineScope,
    peerManager: PeerManager,
    socketInfo: SocketInfo,
    address: String,
) : BindingHolder {
    private val mainJob = mainScope.launch {
        val handler = handlerFactory(address)
        try {
            handler.acquire()
            handler.notify(
                InprocEndpointEvent.Binding(
                    peerManager,
                    socketInfo.options.routingId?.let { Identity(it) }) {
                    val randomId = Random.Default.nextBytes(8).encodeBase64()
                    PeerMailbox("$address/$randomId", socketInfo.options)
                })

            awaitCancellation()
        } finally {
            handler.notify(InprocEndpointEvent.Unbinding)
            handler.release()
        }
    }

    override fun close() {
        mainJob.cancel()
    }
}

internal class InprocConnectionHolder(
    handlerFactory: (endpoint: String) -> InprocEndpointHandler,
    mainScope: CoroutineScope,
    private val lingerScope: CoroutineScope,
    peerManager: PeerManager,
    socketInfo: SocketInfo,
    address: String,
) : ConnectionHolder {

    private val mainJob = mainScope.launch {
        val mailbox = PeerMailbox(address, socketInfo.options)

        val handler = handlerFactory(address)
        try {
            handler.acquire()
            peerManager.notify(PeerEvent(ADDITION, mailbox))
            handler.notify(InprocEndpointEvent.Connecting(mailbox, peerManager,
                socketInfo.options.routingId?.let { Identity(it) }
            ))

            awaitCancellation()
        } finally {
            handler.notify(InprocEndpointEvent.Disconnecting(mailbox))
            peerManager.notify(PeerEvent(REMOVAL, mailbox))
            handler.release()
        }
    }

    override fun close() {
        lingerScope.launch {
            mainJob.cancelAndJoin()
        }
    }
}
