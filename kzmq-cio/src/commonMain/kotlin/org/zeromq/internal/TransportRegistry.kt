/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal class TransportRegistry(private val transports: List<Transport>) {
    fun getTransportFor(endpoint: String): Transport {
        val scheme = endpoint.substringBefore("://")
        return transports.find { it.supportsSchemes(scheme) }
            ?: error("Transport not supported: $scheme")
    }

    fun close() {
        transports.forEach { it.close() }
    }
}
