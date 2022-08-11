/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package org.zeromq.internal

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.channels.*
import org.zeromq.*

internal val GREETING_PART1_SIZE = SIGNATURE.size + 1
internal val GREETING_PART2_SIZE = 1 + MECHANISM_SIZE + 1 + FILLER.size

internal suspend fun ByteReadChannel.readGreetingPart1(): Int = wrapExceptions {
    return readPacket(GREETING_PART1_SIZE).run {
        if (readUByte() != signatureHeadByte) invalidFrame("Invalid signature header byte")
        discardExact(8)
        if (readUByte() != signatureTrailByte) invalidFrame("Invalid signature header byte")
        readByte().toInt()
    }
}

internal suspend fun ByteReadChannel.readGreetingPart2(): Pair<Int, SecuritySpec> = wrapExceptions {
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

internal suspend fun ByteReadChannel.readCommandOrMessage(): CommandOrMessage = wrapExceptions {
    val flags = readZmqFlags()
    return if (flags.isCommand) CommandOrMessage(readCommandContent(flags))
    else CommandOrMessage(readMessageContent(flags))
}

internal suspend fun ByteReadChannel.readCommand(): Command = wrapExceptions {
    val flags = readZmqFlags()
    if (!flags.isCommand) invalidFrame("Expected command")
    return readCommandContent(flags)
}

private suspend fun ByteReadChannel.readCommandContent(flags: ZmqFlags): Command {
    val size = readSize(flags)
    return readRemaining(size).readCommandContent()
}

private fun ByteReadPacket.readCommandContent(): Command {
    return when (CommandName.find(readShortString())) {
        null -> invalidFrame("Invalid command name: ${readShortString()}")
        CommandName.READY -> ReadyCommand(readProperties())
        CommandName.ERROR -> ErrorCommand(readShortString())
        CommandName.SUBSCRIBE -> SubscribeCommand(readBytes())
        CommandName.CANCEL -> CancelCommand(readBytes())
        CommandName.PING -> PingCommand(readShort().toUShort(), readBytes())
        CommandName.PONG -> PongCommand(readBytes())
    }
}

private fun ByteReadPacket.readProperties(): Map<PropertyName, ByteArray> {
    val properties = mutableMapOf<PropertyName, ByteArray>()
    while (remaining > 0) {
        val (propertyName, value) = readProperty()
        properties[propertyName] = value
    }
    return properties
}

private fun ByteReadPacket.readProperty(): Pair<PropertyName, ByteArray> {
    val propertyNameString = readShortString()
    val propertyName = PropertyName.find(propertyNameString) ?: invalidFrame("Can't read property")
    val valueSize = readInt()
    val valueBytes = readBytes(valueSize)
    return propertyName to valueBytes
}

private fun ByteReadPacket.readShortString(): String {
    val size = readUByte().toInt()
    return readBytes(size).decodeToString()
}

private suspend fun ByteReadChannel.readMessageContent(initialFlags: ZmqFlags): Message {
    var flags = initialFlags
    val parts = mutableListOf<ByteArray>()

    do {
        if (flags.isCommand) invalidFrame("Expected message")

        parts.add(readMessagePartContent(flags))

        val hasMore = flags.isMore
        if (hasMore) flags = readZmqFlags()
    } while (hasMore)

    return Message(parts)
}

private suspend fun ByteReadChannel.readMessagePartContent(flags: ZmqFlags): ByteArray {
    val size = readSize(flags)
    return readRemaining(size).readBytes()
}

private suspend fun ByteReadChannel.readSize(flags: ZmqFlags): Long {
    return if (!flags.isLongSize) readUByte().toLong() else readULong().toLong()
}

private suspend inline fun ByteReadChannel.readZmqFlags() = ZmqFlags(readUByte())
private suspend fun ByteReadChannel.readULong() = readLong().toULong()
private suspend inline fun ByteReadChannel.readUByte() = readByte().toUByte()

internal inline fun <T> ByteReadChannel.wrapExceptions(block: ByteReadChannel.() -> T): T {
    try {
        return block()
    } catch (e: ClosedReceiveChannelException) {
        throw IOException(e.message ?: "Error while reading", e)
    }
}
