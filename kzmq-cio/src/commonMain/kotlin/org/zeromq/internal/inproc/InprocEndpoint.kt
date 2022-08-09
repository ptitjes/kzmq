/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.inproc

internal class InprocEndpoint(val name: String) {
    override fun toString(): String = "inproc://$name"
}

internal fun parseInprocEndpointOrNull(endpoint: String): InprocEndpoint? {
    if (!endpoint.startsWith("inproc://")) return null
    val name = endpoint.substringAfter("inproc://")
    return InprocEndpoint(name)
}

internal fun parseInprocEndpoint(endpoint: String): InprocEndpoint =
    parseInprocEndpointOrNull(endpoint) ?: error("Should be an 'inproc://' address")
