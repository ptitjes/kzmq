/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.tcp

import io.ktor.network.sockets.*

internal class TcpEndpoint(val address: SocketAddress) {

    override fun toString(): String = when (address) {
        is InetSocketAddress -> "tcp://${address.hostname}:${address.port}"
        is UnixSocketAddress -> "ipc://${address.path}"
        else -> error("Unknown SocketAddress type")
    }
}

internal fun parseTcpEndpointOrNull(endpoint: String): TcpEndpoint? {
    val scheme = endpoint.substringBefore("://")
    val withoutScheme = endpoint.substringAfter("://")
    return when (scheme) {
        "tcp" -> TcpEndpoint(
            InetSocketAddress(
                withoutScheme.substringBefore(':'),
                withoutScheme.substringAfter(':').toInt()
            )
        )

        "ipc" -> TcpEndpoint(UnixSocketAddress(withoutScheme))

        else -> null
    }
}

internal fun parseTcpEndpoint(endpoint: String): TcpEndpoint =
    parseTcpEndpointOrNull(endpoint) ?: error("Should be a 'tcp://' or 'ipc://' address")
