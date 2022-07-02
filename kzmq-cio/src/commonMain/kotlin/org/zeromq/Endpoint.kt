/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.ktor.network.sockets.*
import org.zeromq.internal.*

internal sealed interface Endpoint

internal data class TCPEndpoint(
    val hostname: String,
    val port: Int,
    val path: String? = null,
) : Endpoint {
    override fun toString(): String {
        return "$TCP_PREFIX$hostname:$port${path ?: ""}"
    }
}

internal data class IPCEndpoint(
    val path: String,
) : Endpoint {
    override fun toString(): String {
        return "$IPC_PREFIX$path"
    }
}

private const val TCP_PREFIX = "tcp://"
private const val IPC_PREFIX = "ipc://"

internal fun parseEndpointOrNull(endpoint: String): Endpoint? {
    if (endpoint.startsWith(TCP_PREFIX)) {
        val withoutProtocol = endpoint.removePrefix(TCP_PREFIX)
        val noPath = !withoutProtocol.contains('/')

        val hostname = withoutProtocol.substringBefore(':')
        val beforePath = if (noPath) withoutProtocol else withoutProtocol.substringBefore('/')
        val port = beforePath.substringAfter(':').toInt()
        val path = if (noPath) null else "/${withoutProtocol.substringAfter('/')}"

        return TCPEndpoint(hostname, port, path)
    } else if (endpoint.startsWith(IPC_PREFIX)) {
        val path = endpoint.removePrefix(IPC_PREFIX)
        return IPCEndpoint(path)
    }

    return null
}

internal fun parseEndpoint(endpoint: String): Endpoint =
    parseEndpointOrNull(endpoint) ?: protocolError("Endpoint not supported: $endpoint")

internal fun SocketAddress.toEndpoint() = when (this) {
    is InetSocketAddress -> TCPEndpoint(hostname, port)
    is UnixSocketAddress -> IPCEndpoint(path)
}
