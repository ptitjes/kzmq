/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.utils.io.*

internal suspend fun nullMechanismHandshake(
    localProperties: MutableMap<PropertyName, ByteArray>,
    isServer: Boolean,
    input: ByteReadChannel,
    output: ByteWriteChannel,
): Map<PropertyName, ByteArray> {
    return if (isServer) {
        logger.v { "Expecting READY command" }
        val properties = expectReadyCommand(input)
        logger.v { "Sending READY command" }
        output.sendReadyCommand(localProperties)
        properties
    } else {
        logger.v { "Sending READY command" }
        output.sendReadyCommand(localProperties)
        logger.v { "Expecting READY command" }
        expectReadyCommand(input)
    }
}

private suspend fun expectReadyCommand(input: ByteReadChannel): Map<PropertyName, ByteArray> {
    return when (val command = input.readCommand()) {
        is ReadyCommand -> command.properties
        is ErrorCommand -> fatalProtocolError("Peer error occurred: ${command.reason}")
        else -> protocolError("Expected READY or ERROR, but got ${command.name}")
    }
}

private suspend fun ByteWriteChannel.sendReadyCommand(properties: MutableMap<PropertyName, ByteArray>) {
    writeCommand(ReadyCommand(properties))
}
