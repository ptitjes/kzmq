/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.tcp

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import org.zeromq.internal.*
import kotlin.coroutines.*

internal class TcpTransport(
    coroutineContext: CoroutineContext,
) : Transport {
    private val selectorManager = SelectorManager(coroutineContext)
    private val rawSocketBuilder = aSocket(selectorManager)

    override fun supportsSchemes(scheme: String) = scheme == "tcp" || scheme == "ipc"
    override val isMulticast = false

    override fun close() {
        selectorManager.close()
    }

    override suspend fun bind(
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        endpoint: String,
    ) = coroutineScope {
        val address = parseTcpEndpoint(endpoint).address
        val rawServerSocket = rawSocketBuilder.tcp().bind(address)

        while (isActive) {
            val rawSocket = rawServerSocket.accept()
            launch {
                val remoteEndpoint = TcpEndpoint(rawSocket.remoteAddress).toString()
                val mailbox = PeerMailbox(remoteEndpoint, socketInfo.options)
                logger.d { "Accepting peer $mailbox" }
                try {
                    val socketHandler = TcpSocketHandler(socketInfo, true, mailbox, rawSocket)
                    socketHandler.handleInitialization()

                    try {
                        peerManager.notify(PeerEvent.Kind.ADDITION, mailbox)
                        peerManager.notify(PeerEvent.Kind.CONNECTION, mailbox)
                        socketHandler.handleTraffic()
                    } finally {
                        peerManager.notify(PeerEvent.Kind.DISCONNECTION, mailbox)
                        peerManager.notify(PeerEvent.Kind.REMOVAL, mailbox)
                    }
                } catch (t: Throwable) {
                    logger.d { "Peer disconnected [${t.message}]" }
                } finally {
                    rawSocket.close()
                }
            }
        }
    }

    override suspend fun connect(
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        endpoint: String,
    ) = coroutineScope {
        val mailbox = PeerMailbox(endpoint, socketInfo.options)

        try {
            peerManager.notify(PeerEvent.Kind.ADDITION, mailbox)

            while (isActive) {
                var rawSocket: Socket? = null
                try {
                    val remoteAddress = parseTcpEndpoint(endpoint).address
                    rawSocket = rawSocketBuilder.tcp().connect(remoteAddress)
                    val socketHandler = TcpSocketHandler(socketInfo, false, mailbox, rawSocket)
                    socketHandler.handleInitialization()
                    peerManager.notify(PeerEvent.Kind.CONNECTION, mailbox)
                    socketHandler.handleTraffic()
                } catch (e: CancellationException) {
                    // Ignore
                } catch (t: Throwable) {
                    logger.d { "Failed to connect [${t.message}]" }
                } finally {
                    peerManager.notify(PeerEvent.Kind.DISCONNECTION, mailbox)
                    rawSocket?.close()
                }

                // TODO Wait before reconnecting ? (have a strategy)
            }
        } finally {
            peerManager.notify(PeerEvent.Kind.REMOVAL, mailbox)
        }
    }
}
