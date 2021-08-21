package org.zeromq.wire

import io.ktor.utils.io.*
import org.zeromq.Type

internal suspend fun nullMechanismHandshake(
    socketType: Type,
    isServer: Boolean,
    input: ByteReadChannel,
    output: ByteWriteChannel
) {
    if (isServer) {
        val properties = expectReadyCommand(input)
        validateSocketType(properties, socketType)
        output.sendReadyCommand(socketType)
    } else {
        output.sendReadyCommand(socketType)
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

private fun validateSocketType(
    properties: Map<PropertyName, ByteArray>,
    socketType: Type
) {
    val socketTypeProperty = (properties[PropertyName.SOCKET_TYPE]
        ?: protocolError("No socket type property in metadata"))
    val peerSocketType = findSocketType(socketTypeProperty.decodeToString())
    if (peerSocketType != socketType) protocolError("Invalid socket type")
}

private fun findSocketType(socketTypeString: String): Type? =
    Type.values().find { it.name == socketTypeString.uppercase() }
