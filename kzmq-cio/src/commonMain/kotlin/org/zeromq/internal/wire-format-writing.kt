/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package org.zeromq.internal

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import org.zeromq.*

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

private val filler = UByteArray(31) { NULL }

internal suspend fun ByteWriteChannel.writeGreetingPart1() =
    writePacket {
        writeFully(signature)
        writeUByte(MAJOR_VERSION.toUByte())
    }

internal suspend fun ByteWriteChannel.writeGreetingPart2(mechanism: Mechanism, asServer: Boolean) =
    writePacket {
        writeUByte(MINOR_VERSION.toUByte())
        writeMechanism(mechanism)
        writeUByte(if (asServer) AS_SERVER else AS_CLIENT)
        writeFully(filler)
    }

private fun BytePacketBuilder.writeMechanism(mechanism: Mechanism) {
    val bytes = mechanism.bytes
    writeFully(bytes)
    repeat(20 - bytes.size) { writeUByte(NULL) }
}

internal suspend fun ByteWriteChannel.writeCommandOrMessage(commandOrMessage: CommandOrMessage) {
    when (commandOrMessage) {
        is CommandCase -> writeCommand(commandOrMessage.command)
        is MessageCase -> writeMessage(commandOrMessage.message)
    }
}

private suspend fun ByteWriteChannel.writeCommand(command: Command) {
    when (command) {
        is ReadyCommand -> writeCommand(command)
        is ErrorCommand -> writeCommand(command)
        is SubscribeCommand -> writeCommand(command)
        is CancelCommand -> writeCommand(command)
        is PingCommand -> writeCommand(command)
        is PongCommand -> writeCommand(command)
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

internal suspend fun ByteWriteChannel.writeCommand(command: SubscribeCommand) = writePacket {
    writeCommand(CommandName.SUBSCRIBE) {
        writeFully(command.topic)
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: CancelCommand) = writePacket {
    writeCommand(CommandName.CANCEL) {
        writeFully(command.topic)
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: PingCommand) = writePacket {
    writeCommand(CommandName.PING) {
        writeUShort(command.ttl)
        writeFully(command.context)
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: PongCommand) = writePacket {
    writeCommand(CommandName.PONG) {
        writeFully(command.context)
    }
}

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

private fun BytePacketBuilder.writeProperty(
    propertyName: PropertyName,
    valueBytes: ByteArray
) {
    writeShortString(propertyName.bytes)
    writeInt(valueBytes.size)
    writeFully(valueBytes)
}

private fun BytePacketBuilder.writeShortString(bytes: ByteArray) {
    writeUByte(bytes.size.toUByte())
    writeFully(bytes)
}

private suspend fun ByteWriteChannel.writeMessage(message: Message) = writePacket {
    val parts = message.parts
    val lastIndex = parts.size - 1
    for ((index, part) in parts.withIndex()) {
        val hasMore = index < lastIndex
        writeMessagePart(hasMore, part)
    }
}

private fun BytePacketBuilder.writeMessagePart(hasMore: Boolean, part: ByteArray) {
    val messageFlag: UByte = if (hasMore) FLAG_MORE else NULL
    writeFrameHeader(messageFlag, part.size.toLong())
    writeFully(part)
}

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
