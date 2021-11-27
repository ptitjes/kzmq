/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import org.zeromq.*

private val characters =
    listOf('_') + ('a'..'z').toList() + ('A'..'Z').toList() + ('0'..'9').toList()

enum class Protocol {
    INPROC,
    IPC,
    TCP,
}

fun randomAddress(protocol: Protocol = Protocol.TCP): String {
    return when (protocol) {
        Protocol.INPROC -> "inproc://${randomAddressSuffix()}"
        Protocol.IPC -> "ipc:///tmp/${randomAddressSuffix()}"
        Protocol.TCP -> "tcp://localhost:${findOpenPort()}"
    }
}

private fun randomAddressSuffix() = List(16) { characters.random() }.joinToString("")

fun generateMessages(messageCount: Int) = List(messageCount) { Message("message-$it".encodeToByteArray()) }
