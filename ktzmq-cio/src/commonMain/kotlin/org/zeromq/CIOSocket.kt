package org.zeromq

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.zeromq.EndpointActionKind.ADDITION
import org.zeromq.EndpointActionKind.REMOVAL
import org.zeromq.EndpointActionSource.BIND
import org.zeromq.EndpointActionSource.CONNECT
import org.zeromq.wire.RawSocket
import org.zeromq.wire.protocolError
import org.zeromq.wire.startRawSocket
import kotlin.coroutines.CoroutineContext

internal abstract class CIOSocket(
    final override val coroutineContext: CoroutineContext,
    selectorManager: SelectorManager,
    final override val type: Type
) : Socket, CoroutineScope {

    private val rawSocketHandler = RawSocketHandler(coroutineContext, selectorManager, type)
    protected val rawSocketActions: ReceiveChannel<RawSocketAction>
        get() = rawSocketHandler.rawSocketActions

    override fun close() {
        val job = coroutineContext[Job]!!
        job.cancel()
    }

    override suspend fun bind(endpoint: String) {
        val parsedEndpoint = parseEndpoint(endpoint)
            ?: error("Endpoint not supported: $endpoint")

        rawSocketHandler.endpointActions.send(EndpointAction(ADDITION, BIND, parsedEndpoint))
    }

    override suspend fun unbind(endpoint: String) {
        val parsedEndpoint = parseEndpoint(endpoint)
            ?: error("Endpoint not supported: $endpoint")

        rawSocketHandler.endpointActions.send(EndpointAction(REMOVAL, BIND, parsedEndpoint))
    }

    override suspend fun connect(endpoint: String) {
        val parsedEndpoint = parseEndpoint(endpoint)
            ?: protocolError("Endpoint not supported: $endpoint")

        rawSocketHandler.endpointActions.send(EndpointAction(ADDITION, CONNECT, parsedEndpoint))
    }

    override suspend fun disconnect(endpoint: String) {
        val parsedEndpoint = parseEndpoint(endpoint)
            ?: protocolError("Endpoint not supported: $endpoint")

        rawSocketHandler.endpointActions.send(EndpointAction(REMOVAL, CONNECT, parsedEndpoint))
    }
}

internal data class RawSocketAction(
    val kind: RawSocketActionKind,
    val rawSocket: RawSocket,
)

internal enum class RawSocketActionKind { ADDITION, REMOVAL }

internal class RawSocketHandler(
    override val coroutineContext: CoroutineContext,
    private val selectorManager: SelectorManager,
    private val type: Type,
) : CoroutineScope {

    private val _endpointActions = Channel<EndpointAction>()
    val endpointActions: SendChannel<EndpointAction> get() = _endpointActions

    private val _rawSocketActions = Channel<RawSocketAction>()
    val rawSocketActions: ReceiveChannel<RawSocketAction> get() = _rawSocketActions

    private val serverJobs = mutableMapOf<Endpoint, Job>()

    private val connectRawSockets = hashMapOf<Endpoint, RawSocket>()

    init {
        launch {
            for (action in _endpointActions) {
                val endpoint = action.endpoint
                when (action.source) {
                    BIND -> when (action.kind) {
                        ADDITION -> if (!serverJobs.containsKey(endpoint)) bind(endpoint)
                        REMOVAL -> if (serverJobs.containsKey(endpoint)) unbind(endpoint)
                    }
                    CONNECT -> when (action.kind) {
                        ADDITION -> if (!connectRawSockets.containsKey(endpoint)) connect(endpoint)
                        REMOVAL -> if (connectRawSockets.containsKey(endpoint)) disconnect(endpoint)
                    }
                }
            }
        }
    }

    private fun bind(endpoint: Endpoint) {
        val server = when (endpoint) {
            is TCPEndpoint -> {
                aSocket(selectorManager).tcp().bind(endpoint.hostname, endpoint.port)
            }
        }

        serverJobs[endpoint] = launch {
            val rawSockets = hashMapOf<Endpoint, RawSocket>()

            while (isActive) {
                val socket = server.accept()

                val remoteEndpoint = socket.remoteAddress.toTCPEndpoint()

                // TODO this is not the correct coroutineContext, right?
                val rawSocket = startRawSocket(coroutineContext, socket, type, true)
                rawSockets[remoteEndpoint] = rawSocket
                notifyRawSocketAddition(rawSocket)
            }

            for ((_, rawSocket) in rawSockets) {
                notifyRawSocketRemoval(rawSocket)
            }
        }
    }

    private fun unbind(endpoint: Endpoint) {
        val job = serverJobs.remove(endpoint)
        job?.cancel()
    }

    private suspend fun connect(endpoint: Endpoint) {
        val socket = when (endpoint) {
            is TCPEndpoint -> {
                aSocket(selectorManager).tcp().connect(endpoint.hostname, endpoint.port)
            }
        }

        val rawSocket = startRawSocket(coroutineContext, socket, type, false)
        connectRawSockets[endpoint] = rawSocket
        notifyRawSocketAddition(rawSocket)
    }

    private suspend fun disconnect(endpoint: Endpoint) {
        val rawSocket = connectRawSockets.remove(endpoint)
        if (rawSocket != null) notifyRawSocketRemoval(rawSocket)
    }

    private suspend fun notifyRawSocketAddition(rawSocket: RawSocket) {
        _rawSocketActions.send(RawSocketAction(RawSocketActionKind.ADDITION, rawSocket))
    }

    private suspend fun notifyRawSocketRemoval(rawSocket: RawSocket) {
        _rawSocketActions.send(RawSocketAction(RawSocketActionKind.REMOVAL, rawSocket))
    }
}

private fun NetworkAddress.toTCPEndpoint() = TCPEndpoint(hostname, port)

internal data class EndpointAction(
    val kind: EndpointActionKind,
    val source: EndpointActionSource,
    val endpoint: Endpoint,
)

internal enum class EndpointActionKind { ADDITION, REMOVAL }
internal enum class EndpointActionSource { BIND, CONNECT }
