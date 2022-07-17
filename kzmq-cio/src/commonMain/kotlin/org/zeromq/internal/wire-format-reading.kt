/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package org.zeromq.internal

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import org.zeromq.*

internal val GREETING_PART1_SIZE = SIGNATURE.size + 1
internal val GREETING_PART2_SIZE = 1 + MECHANISM_SIZE + 1 + FILLER.size

internal suspend fun ByteReadChannel.readGreetingPart1(): Int {
    return readPacket(GREETING_PART1_SIZE).run {
        if (readUByte() != signatureHeadByte) invalidFrame("Invalid signature header byte")
        discardExact(8)
        if (readUByte() != signatureTrailByte) invalidFrame("Invalid signature header byte")
        readByte().toInt()
    }
}

internal suspend fun ByteReadChannel.readGreetingPart2(): Pair<Int, SecuritySpec> {
    return readPacket(GREETING_PART2_SIZE).run {
        val minorVersion = readByte().toInt()
        val mechanism = readMechanism()
        val asServer = readUByte() == AS_SERVER
        discardExact(FILLER.size)
        Pair(minorVersion, SecuritySpec(mechanism, asServer))
    }
}

private fun ByteReadPacket.readMechanism(): Mechanism {
    val bytes = readBytes(MECHANISM_SIZE)
    val fullString = bytes.decodeToString()
    val string = fullString.substring(0, fullString.indexOf('\u0000'))
    return Mechanism.values().find { it.name == string } ?: invalidFrame("Invalid security mechanism")
}

internal suspend fun ByteReadChannel.readCommandOrMessage(): CommandOrMessage {
    val flags = readZmqFlags()
    return if (flags.isCommand) CommandOrMessage(readCommandContent(flags))
    else CommandOrMessage(readMessageContent(flags))
}

internal suspend fun ByteReadChannel.readCommand(): Command {
    val flags = readZmqFlags()
    if (!flags.isCommand) invalidFrame("Expected command")
    return readCommandContent(flags)
}

private suspend fun ByteReadChannel.readCommandContent(flags: ZmqFlags): Command {
    val size = if (!flags.isLongSize) {
        readUByte().toLong()
    } else {
        readLong().toULong().toLong()
    }

    val commandNameString = readShortString()
    val commandName = CommandName.find(commandNameString) ?: invalidFrame("Invalid command name: $commandNameString")

    val remaining = size - (1 + commandNameString.length)

    return when (commandName) {
        CommandName.READY -> ReadyCommand(readProperties(remaining))
        CommandName.ERROR -> ErrorCommand(readShortString())
        CommandName.SUBSCRIBE -> SubscribeCommand(readBytes(remaining.toInt()))
        CommandName.CANCEL -> CancelCommand(readBytes(remaining.toInt()))
        CommandName.PING -> PingCommand(readShort().toUShort(), readBytes(remaining.toInt() - 2))
        CommandName.PONG -> PongCommand(readBytes(remaining.toInt()))
    }
}

private suspend fun ByteReadChannel.readMessageContent(initialFlags: ZmqFlags): Message {
    var flags = initialFlags
    val frames = mutableListOf<Frame>()

    do {
        if (flags.isCommand) invalidFrame("Expected message")

        frames.add(readMessageFrame(flags))

        val hasMore = flags.isMore
        if (hasMore) flags = readZmqFlags()
    } while (hasMore)

    return messageOf(frames)
}

private suspend fun ByteReadChannel.readMessageFrame(flags: ZmqFlags): Frame {
    // FIXME cast size to int for now
    val size = (if (!flags.isLongSize) readUByte().toInt() else readULong().toInt())

    // TODO use a pool
    val bytes = readBytes(size)
    return constantFrameOf(bytes)
}

private suspend fun ByteReadChannel.readProperties(dataSize: Long): Map<PropertyName, ByteArray> {
    var remaining = dataSize
    val properties = mutableMapOf<PropertyName, ByteArray>()
    while (remaining > 0) {
        val (propertyName, value) = readProperty()
        properties[propertyName] = value
        remaining -= 1 + propertyName.bytes.size + 4 + value.size
    }
    return properties
}

private suspend fun ByteReadChannel.readProperty(): Pair<PropertyName, ByteArray> {
    val propertyNameString = readShortString()
    val propertyName = PropertyName.find(propertyNameString) ?: invalidFrame("Can't read property")
    val valueSize = readInt()
    val valueBytes = readBytes(valueSize)
    return propertyName to valueBytes
}

private suspend fun ByteReadChannel.readShortString(): String {
    val size = readUByte().toInt()
    return readBytes(size).decodeToString()
}

private suspend fun ByteReadChannel.readBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    readFully(bytes)
    return bytes
}

private suspend inline fun ByteReadChannel.readUByte() = readByte().toUByte()
private suspend inline fun ByteReadChannel.readULong() = readLong().toULong()

private suspend inline fun ByteReadChannel.readZmqFlags() = ZmqFlags(readUByte())
