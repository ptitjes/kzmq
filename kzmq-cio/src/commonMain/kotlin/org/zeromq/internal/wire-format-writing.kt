/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.writeUByte
import kotlinx.io.*
import kotlinx.io.Buffer
import org.zeromq.*

private suspend inline fun ByteWriteChannel.write(writer: Sink.() -> Unit) {
    val buffer = Buffer().apply(writer)
    writeBuffer(buffer)
    flush()
}

@OptIn(ExperimentalUnsignedTypes::class)
internal suspend fun ByteWriteChannel.writeGreetingPart1() =
    write {
        write(SIGNATURE.asByteArray())
        writeUByte(MAJOR_VERSION.toUByte())
    }

@OptIn(ExperimentalUnsignedTypes::class)
internal suspend fun ByteWriteChannel.writeGreetingPart2(mechanism: Mechanism, asServer: Boolean) =
    write {
        writeUByte(MINOR_VERSION.toUByte())
        writeMechanism(mechanism)
        writeUByte(if (asServer) AS_SERVER else AS_CLIENT)
        write(FILLER.asByteArray())
    }

private fun Sink.writeMechanism(mechanism: Mechanism) {
    val bytes = mechanism.bytes
    write(bytes)
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

internal suspend fun ByteWriteChannel.writeCommand(command: ReadyCommand) = write {
    writeCommand(CommandName.READY) {
        for ((propertyName, bytes) in command.properties) {
            writeProperty(propertyName, bytes)
        }
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: ErrorCommand) = write {
    writeCommand(CommandName.READY) {
        writeShortString(command.reason.encodeToByteArray())
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: SubscribeCommand) = write {
    writeCommand(CommandName.SUBSCRIBE) {
        writeFully(command.topic)
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: CancelCommand) = write {
    writeCommand(CommandName.CANCEL) {
        writeFully(command.topic)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
internal suspend fun ByteWriteChannel.writeCommand(command: PingCommand) = write {
    writeCommand(CommandName.PING) {
        writeUShort(command.ttl)
        writeFully(command.context)
    }
}

internal suspend fun ByteWriteChannel.writeCommand(command: PongCommand) = write {
    writeCommand(CommandName.PONG) {
        writeFully(command.context)
    }
}

private fun Sink.writeCommand(
    commandName: CommandName,
    dataBuilder: Sink.() -> Unit,
) {
    val commandBody = Buffer().apply {
        writeShortString(commandName.bytes)
        dataBuilder()
    }

    writeFrameHeader(ZmqFlags.command, commandBody.remaining)
    writePacket(commandBody)
}

private fun Sink.writeProperty(
    propertyName: PropertyName,
    valueBytes: ByteArray,
) {
    writeShortString(propertyName.bytes)
    writeInt(valueBytes.size)
    writeFully(valueBytes)
}

private fun Sink.writeShortString(bytes: ByteArray) {
    writeUByte(bytes.size.toUByte())
    writeFully(bytes)
}

private suspend fun ByteWriteChannel.writeMessage(message: Message) = write {
    val parts = message.frames
    val lastIndex = parts.lastIndex
    for ((index, part) in parts.withIndex()) {
        val hasMore = index < lastIndex
        writeMessagePart(hasMore, part)
    }
}

private fun Sink.writeMessagePart(hasMore: Boolean, part: ByteArray) {
    val flags = if (hasMore) ZmqFlags.more else ZmqFlags.none
    writeFrameHeader(flags, part.size.toLong())
    writeFully(part)
}

private fun Sink.writeFrameHeader(flags: ZmqFlags, size: Long) {
    if (size <= 255) {
        writeZmqFlags(flags)
        writeUByte(size.toUByte())
    } else {
        writeZmqFlags(flags + ZmqFlags.longSize)
        writeULong(size.toULong())
    }
}

private fun Sink.writeZmqFlags(flags: ZmqFlags) {
    writeUByte(flags.data)
}

