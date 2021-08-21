package org.zeromq.wire

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import org.zeromq.Message

internal const val NULL: UByte = 0x00u

internal const val signatureHeadByte: UByte = 0xffu
internal const val signatureTrailByte: UByte = 0x7fu

internal const val MAJOR_VERSION = 3
internal const val MINOR_VERSION = 1

internal const val AS_CLIENT: UByte = 0x00u
internal const val AS_SERVER: UByte = 0x01u

internal const val FLAG_MORE: UByte = 0x01u
internal const val FLAG_LONG_SIZE: UByte = 0x02u
internal const val FLAG_COMMAND: UByte = 0x04u

@OptIn(ExperimentalUnsignedTypes::class)
private val signature =
    ubyteArrayOf(
        signatureHeadByte,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        signatureTrailByte
    )

internal enum class Mechanism {
    NULL,
    PLAIN,
    CURVE;

    val bytes: ByteArray = name.encodeToByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
private val filler = UByteArray(31) { NULL }

@OptIn(ExperimentalUnsignedTypes::class)
internal suspend fun ByteWriteChannel.writeGreetingPart1() = writePacket {
    writeFully(signature)
    writeUByte(MAJOR_VERSION.toUByte())
}

@OptIn(ExperimentalUnsignedTypes::class)
internal suspend fun ByteWriteChannel.writeGreetingPart2(mechanism: Mechanism, asServer: Boolean) =
    writePacket {
        writeUByte(MINOR_VERSION.toUByte())
        writeMechanism(mechanism)
        writeUByte(if (asServer) AS_SERVER else AS_CLIENT)
        writeFully(filler)
    }

@OptIn(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeMechanism(mechanism: Mechanism) {
    val bytes = mechanism.bytes
    writeFully(bytes)
    repeat(20 - bytes.size) { writeUByte(NULL) }
}

internal suspend fun ByteWriteChannel.writeCommandOrMessage(commandOrMessage: CommandOrMessage) {
    if (commandOrMessage.isCommand) {
        when (val command = commandOrMessage.commandOrThrow()) {
            is ReadyCommand -> writeCommand(command)
            is ErrorCommand -> writeCommand(command)
            else -> TODO()
        }
    } else {
        val message = commandOrMessage.messageOrThrow()
        writeMessage(message)
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: ReadyCommand) = writePacket {
    writeCommand(CommandName.READY) {
        for ((propertyName, bytes) in command.properties) {
            writeProperty(propertyName, bytes)
        }
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: ErrorCommand) = writePacket {
    writeCommand(CommandName.READY) {
        writeShortString(command.reason.encodeToByteArray())
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeCommand(
    commandName: CommandName,
    dataBuilder: BytePacketBuilder.() -> Unit
) {
    val commandBody = buildPacket {
        writeShortString(commandName.bytes)
        dataBuilder()
    }

    writeFrameHeader(FLAG_COMMAND, commandBody.remaining)
    writePacket(commandBody)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeProperty(
    propertyName: PropertyName,
    valueBytes: ByteArray
) {
    writeShortString(propertyName.bytes)
    writeInt(valueBytes.size)
    writeFully(valueBytes)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeShortString(bytes: ByteArray) {
    writeUByte(bytes.size.toUByte())
    writeFully(bytes)
}

@OptIn(ExperimentalUnsignedTypes::class)
internal suspend fun ByteWriteChannel.writeMessage(message: Message) {
    val parts = message.parts
    val lastIndex = parts.size - 1
    for ((index, part) in parts.withIndex()) {
        val hasMore = index < lastIndex
        writePacket {
            writeMessagePart(part, hasMore)
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeMessagePart(part: ByteArray, hasMore: Boolean) {

    val messageBody = buildPacket {
        writeFully(part)
    }

    val messageFlag: UByte = if (hasMore) FLAG_MORE else NULL
    writeFrameHeader(messageFlag, messageBody.remaining)
    writePacket(messageBody)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeFrameHeader(
    flags: UByte,
    size: Long
) {
    if (size <= 255) {
        writeUByte(flags)
        writeUByte(size.toUByte())
    } else {
        writeUByte(flags or FLAG_LONG_SIZE)
        writeULong(size.toULong())
    }
}
