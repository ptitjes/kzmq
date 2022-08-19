/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.tcp

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import org.zeromq.internal.PeerEvent.Kind.*
import kotlin.coroutines.*

internal class TcpTransport(
    coroutineContext: CoroutineContext,
) : Transport {
    private val selectorManager = SelectorManager(coroutineContext)
    private val rawSocketBuilder = aSocket(selectorManager)

    override fun supportsSchemes(scheme: String) = scheme == "tcp" || scheme == "ipc"
    override val isMulticast get() = false

    override fun close() {
        selectorManager.close()
    }

    override fun bind(
        mainScope: CoroutineScope,
        lingerScope: CoroutineScope,
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        address: String,
    ): BindingHolder = TcpBindingHolder(
        rawSocketBuilder,
        mainScope,
        peerManager,
        socketInfo,
        address,
    )

    override fun connect(
        mainScope: CoroutineScope,
        lingerScope: CoroutineScope,
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        address: String,
    ): ConnectionHolder = TcpConnectionHolder(
        rawSocketBuilder,
        mainScope,
        lingerScope,
        peerManager,
        socketInfo,
        address,
    )
}

internal class TcpBindingHolder(
    rawSocketBuilder: SocketBuilder,
    mainScope: CoroutineScope,
    peerManager: PeerManager,
    socketInfo: SocketInfo,
    address: String,
) : BindingHolder {
    private val mainJob = mainScope.launch {
        val localAddress = parseTcpEndpoint(address).address
        val rawServerSocket = rawSocketBuilder.tcp().bind(localAddress)

        while (isActive) {
            yield()
            val rawSocket = rawServerSocket.accept()
            launch {
                val remoteEndpoint = TcpEndpoint(rawSocket.remoteAddress).toString()
                val mailbox = PeerMailbox(remoteEndpoint, socketInfo.options)
                logger.d { "Accepting peer $mailbox" }

                val socketHandler = TcpSocketHandler(socketInfo, true, mailbox, rawSocket)

                try {
                    socketHandler.handleInitialization()

                    try {
                        peerManager.notify(PeerEvent(ADDITION, mailbox))
                        peerManager.notify(PeerEvent(CONNECTION, mailbox))
                        socketHandler.handleTraffic()
                    } finally {
                        withContext(NonCancellable) {
                            peerManager.notify(PeerEvent(DISCONNECTION, mailbox))
                            peerManager.notify(PeerEvent(REMOVAL, mailbox))
                        }
                    }
                } catch (e: ProtocolError) {
                    logger.d { "Peer disconnected: ${e.message}" }
                } catch (e: IOException) {
                    logger.d { "Peer disconnected: ${e.message}" }
                } catch (e: ClosedReceiveChannelException) {
                    logger.d { "Peer disconnected: ${e.message}" }
                } finally {
                    socketHandler.close()
                }
            }
        }
    }

    override fun close() {
        mainJob.cancel()
    }
}

internal class TcpConnectionHolder(
    rawSocketBuilder: SocketBuilder,
    mainScope: CoroutineScope,
    private val lingerScope: CoroutineScope,
    peerManager: PeerManager,
    socketInfo: SocketInfo,
    address: String,
) : ConnectionHolder {

    private var lastSocketHandler: TcpSocketHandler? = null

    private val mainJob = mainScope.launch {
        val remoteAddress = parseTcpEndpoint(address).address
        val mailbox = PeerMailbox(address, socketInfo.options)

        var socketHandler: TcpSocketHandler? = null
        var shouldReconnect = true

        try {
            peerManager.notify(PeerEvent(ADDITION, mailbox))

            while (isActive) {
                try {
                    val rawSocket = rawSocketBuilder.tcp().connect(remoteAddress)
                    socketHandler = TcpSocketHandler(socketInfo, false, mailbox, rawSocket)
                    socketHandler.handleInitialization()

                    try {
                        peerManager.notify(PeerEvent(CONNECTION, mailbox))
                        socketHandler.handleTraffic()
                    } finally {
                        withContext(NonCancellable) {
                            peerManager.notify(PeerEvent(DISCONNECTION, mailbox))
                        }
                    }
                } catch (e: IllegalStateException) {
                    if (e is CancellationException) throw e

                    // SelectorManager has been closed while suspending to connect
                    shouldReconnect = false
                    socketHandler?.close()
                    socketHandler = null
                } catch (e: ProtocolError) {
                    shouldReconnect = !e.isFatal
                    socketHandler?.close()
                    socketHandler = null
                } catch (e: IOException) {
                    socketHandler?.close()
                    socketHandler = null
                }

                if (!shouldReconnect) break

                // TODO Wait before reconnecting ? (have a strategy)
                yield()
            }
        } finally {
            withContext(NonCancellable) {
                peerManager.notify(PeerEvent(REMOVAL, mailbox))
            }
            lastSocketHandler = socketHandler

            try {
                mailbox.receiveChannel.close()
            } catch (e: ClosedReceiveChannelException) {
                // Channel may already have been closed
            }
        }
    }

    override fun close() {
        lingerScope.launch {
            mainJob.cancelAndJoin()

            lastSocketHandler?.let {
                it.handleLinger()
                it.close()
            }
        }
    }
}
