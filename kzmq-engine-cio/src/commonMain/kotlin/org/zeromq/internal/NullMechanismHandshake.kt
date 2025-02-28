/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.utils.io.*
import kotlinx.io.bytestring.*

internal suspend fun nullMechanismHandshake(
    localProperties: Map<PropertyName, ByteString>,
    isServer: Boolean,
    input: ByteReadChannel,
    output: ByteWriteChannel,
): Map<PropertyName, ByteString> {
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

private suspend fun expectReadyCommand(input: ByteReadChannel): Map<PropertyName, ByteString> {
    return when (val command = input.readCommand()) {
        is ReadyCommand -> command.properties
        is ErrorCommand -> fatalProtocolError("Peer error occurred: ${command.reason}")
        else -> protocolError("Expected READY or ERROR, but got ${command.name}")
    }
}

private suspend fun ByteWriteChannel.sendReadyCommand(properties: Map<PropertyName, ByteString>) {
    writeCommand(ReadyCommand(properties))
}
