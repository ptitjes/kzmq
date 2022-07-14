/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.sockets.Socket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import kotlin.coroutines.*

internal class PeerManager(
    context: CoroutineContext,
    selectorManager: SelectorManager,
    private val socketType: Type,
    private val peerSocketTypes: Set<Type>,
    private val socketOptions: SocketOptions,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = context + CoroutineName("zmq-socket-handler")

    private val socketBuilder = aSocket(selectorManager)

    private val _peerEvents = Channel<PeerEvent>()
    val peerEvents: ReceiveChannel<PeerEvent> get() = _peerEvents

    private val bindJobs = JobMap<Endpoint>()
    private val connectJobs = JobMap<Endpoint>()

    fun bind(endpoint: String) = bind(parseEndpoint(endpoint))
    fun unbind(endpoint: String) = unbind(parseEndpoint(endpoint))
    fun connect(endpoint: String) = connect(parseEndpoint(endpoint))
    fun disconnect(endpoint: String) = disconnect(parseEndpoint(endpoint))

    private fun bind(endpoint: Endpoint) {
        val serverSocket = socketBuilder.bind(endpoint)
        bindJobs.add(endpoint) { acceptPeers(serverSocket, endpoint) }
    }

    private fun unbind(endpoint: Endpoint) {
        bindJobs.remove(endpoint)
    }

    private fun acceptPeers(serverSocket: ServerSocket, endpoint: Endpoint) =
        launch(CoroutineName("zmq-bind-$endpoint")) {
            while (isActive) {
                val socket = serverSocket.accept()
                val remoteEndpoint = socket.remoteAddress.toEndpoint()
                acceptPeer(remoteEndpoint, socket)
            }
        }

    private fun acceptPeer(remoteEndpoint: Endpoint, socket: Socket) =
        launch(CoroutineName("zmq-accept-${remoteEndpoint}")) {
            val mailbox = PeerMailbox(remoteEndpoint, socketOptions)

            logger.d { "Accepting peer $mailbox" }
            try {
                val peerSocket = PeerSocket(socketType, socketOptions, mailbox, socket)
                peerSocket.handleInitialization(true, peerSocketTypes)

                try {
                    notify(PeerEventKind.ADDITION, mailbox)
                    notify(PeerEventKind.CONNECTION, mailbox)
                    peerSocket.handleTraffic()
                } finally {
                    notify(PeerEventKind.DISCONNECTION, mailbox)
                    notify(PeerEventKind.REMOVAL, mailbox)
                }
            } catch (t: Throwable) {
                logger.d { "Peer disconnected [${t.message}]" }
            } finally {
                socket.close()
            }
        }

    private fun connect(endpoint: Endpoint) {
        connectJobs.add(endpoint) { connectPeer(endpoint) }
    }

    private fun disconnect(endpoint: Endpoint) {
        connectJobs.remove(endpoint)
    }

    private fun connectPeer(endpoint: Endpoint) =
        launch(CoroutineName("zmq-connect-$endpoint")) {
            val mailbox = PeerMailbox(endpoint, socketOptions)

            try {
                notify(PeerEventKind.ADDITION, mailbox)

                while (isActive) {
                    var socket: Socket? = null
                    try {
                        socket = socketBuilder.connect(endpoint)
                        val peerSocket = PeerSocket(socketType, socketOptions, mailbox, socket)
                        peerSocket.handleInitialization(false, peerSocketTypes)
                        notify(PeerEventKind.CONNECTION, mailbox)
                        peerSocket.handleTraffic()
                    } catch (t: Throwable) {
                        logger.d { "Failed to connect [${t.message}]" }
                    } finally {
                        notify(PeerEventKind.DISCONNECTION, mailbox)
                        socket?.close()
                    }

                    // TODO Wait before reconnecting ? (have a strategy)
                }
            } finally {
                notify(PeerEventKind.REMOVAL, mailbox)
            }
        }

    private suspend fun notify(eventKind: PeerEventKind, peerMailbox: PeerMailbox) {
        logger.d { "Notifying peer $eventKind: $peerMailbox" }
        _peerEvents.send(PeerEvent(eventKind, peerMailbox))
    }
}

internal data class PeerEvent(
    val kind: PeerEventKind,
    val peerMailbox: PeerMailbox,
)

internal enum class PeerEventKind { ADDITION, CONNECTION, DISCONNECTION, REMOVAL }

private fun SocketBuilder.bind(endpoint: Endpoint): ServerSocket =
    tcp().bind(endpoint.toSocketAddress())

private suspend fun SocketBuilder.connect(endpoint: Endpoint) =
    tcp().connect(endpoint.toSocketAddress())

private fun Endpoint.toSocketAddress(): SocketAddress = when (this) {
    is TCPEndpoint -> InetSocketAddress(hostname, port)
    is IPCEndpoint -> UnixSocketAddress(path)
}
