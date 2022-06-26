/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.utils.io.*
import org.zeromq.*

internal suspend fun nullMechanismHandshake(
    socketType: Type,
    isServer: Boolean,
    input: ByteReadChannel,
    output: ByteWriteChannel,
): Map<PropertyName, ByteArray> {
    return if (isServer) {
        logger.t { "Expecting READY command" }
        val properties = expectReadyCommand(input)
        logger.t { "Sending READY command" }
        output.sendReadyCommand(socketType)
        properties
    } else {
        logger.t { "Sending READY command" }
        output.sendReadyCommand(socketType)
        logger.t { "Expecting READY command" }
        expectReadyCommand(input)
    }
}

private suspend fun expectReadyCommand(input: ByteReadChannel): Map<PropertyName, ByteArray> {
    return when (val command = input.readCommand()) {
        is ReadyCommand -> command.properties
        is ErrorCommand -> protocolError("Peer error occurred: ${command.reason}")
        else -> protocolError("Expected READY or ERROR, but got ${command.name}")
    }
}

private suspend fun ByteWriteChannel.sendReadyCommand(socketType: Type) {
    writeCommand(
        ReadyCommand(
            PropertyName.SOCKET_TYPE to socketType.name.encodeToByteArray()
        )
    )
}
