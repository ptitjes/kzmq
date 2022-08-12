/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package org.zeromq.internal

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import org.zeromq.*

internal suspend fun ByteWriteChannel.writeGreetingPart1() =
    writePacket {
        writeFully(SIGNATURE)
        writeUByte(MAJOR_VERSION.toUByte())
    }

internal suspend fun ByteWriteChannel.writeGreetingPart2(mechanism: Mechanism, asServer: Boolean) =
    writePacket {
        writeUByte(MINOR_VERSION.toUByte())
        writeMechanism(mechanism)
        writeUByte(if (asServer) AS_SERVER else AS_CLIENT)
        writeFully(FILLER)
    }

private fun BytePacketBuilder.writeMechanism(mechanism: Mechanism) {
    val bytes = mechanism.bytes
    writeFully(bytes)
    repeat(MECHANISM_SIZE - bytes.size) { writeUByte(NULL) }
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
    dataBuilder: BytePacketBuilder.() -> Unit,
) {
    val commandBody = buildPacket {
        writeShortString(commandName.bytes)
        dataBuilder()
    }

    writeFrameHeader(ZmqFlags.command, commandBody.remaining)
    writePacket(commandBody)
}

private fun BytePacketBuilder.writeProperty(
    propertyName: PropertyName,
    valueBytes: ByteArray,
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
    val parts = message.frames
    val lastIndex = parts.lastIndex
    for ((index, part) in parts.withIndex()) {
        val hasMore = index < lastIndex
        writeMessagePart(hasMore, part)
    }
}

private fun BytePacketBuilder.writeMessagePart(hasMore: Boolean, part: ByteArray) {
    val flags = if (hasMore) ZmqFlags.more else ZmqFlags.none
    writeFrameHeader(flags, part.size.toLong())
    writeFully(part)
}

private fun BytePacketBuilder.writeFrameHeader(flags: ZmqFlags, size: Long) {
    if (size <= 255) {
        writeZmqFlags(flags)
        writeUByte(size.toUByte())
    } else {
        writeZmqFlags(flags + ZmqFlags.longSize)
        writeULong(size.toULong())
    }
}

private fun BytePacketBuilder.writeZmqFlags(flags: ZmqFlags) {
    writeUByte(flags.data)
}

