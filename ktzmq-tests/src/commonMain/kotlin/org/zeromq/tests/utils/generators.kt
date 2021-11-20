package org.zeromq.tests.utils

import org.zeromq.*
import kotlin.random.*

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
        Protocol.TCP -> "tcp://localhost:${randomTcpPort()}"
    }
}

private fun randomAddressSuffix() = List(16) { characters.random() }.joinToString("")

private fun randomTcpPort() = Random.nextInt(49152, 65536)

fun generateMessages(messageCount: Int) =
    List(messageCount) { Message("message-$it".encodeToByteArray()) }
