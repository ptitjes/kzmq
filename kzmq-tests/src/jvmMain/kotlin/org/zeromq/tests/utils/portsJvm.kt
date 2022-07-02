/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.ktor.network.selector.*
import io.ktor.network.sockets.*

private val TEST_SELECTOR_MANAGER = SelectorManager()

actual suspend fun findOpenPort(): Int =
    aSocket(TEST_SELECTOR_MANAGER).tcp().bind().use {
        val inetAddress = it.localAddress as? InetSocketAddress ?: error("Expected inet socket address")
        inetAddress.port
    }
