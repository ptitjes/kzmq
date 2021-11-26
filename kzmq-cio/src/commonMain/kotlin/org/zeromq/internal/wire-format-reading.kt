/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.utils.io.*
import org.zeromq.*

internal suspend fun ByteReadChannel.readGreetingPart1(): Int {
    if (readByte().toUByte() != signatureHeadByte) invalidFrame("Invalid signature header byte")
    discardExact(8)
    if (readByte().toUByte() != signatureTrailByte) invalidFrame("Invalid signature header byte")
    return readByte().toInt()
}

internal suspend fun ByteReadChannel.readGreetingPart2(): Pair<Int, SecuritySpec> {
    val minorVersion = readByte().toInt()
    val mechanism = readMechanism()
    val asServer = readByte().toUByte() == AS_SERVER
    discardExact(31)
    return Pair(minorVersion, SecuritySpec(mechanism, asServer))
}

private suspend fun ByteReadChannel.readMechanism(): Mechanism {
    val bytes = readBytes(20)
    val fullString = bytes.decodeToString()
    val string = fullString.substring(0, fullString.indexOf('\u0000'))
    return Mechanism.values().find { it.name == string }
        ?: invalidFrame("Invalid security mechanism")
}

internal suspend fun ByteReadChannel.readCommandOrMessage(): CommandOrMessage {
    val flags = readByte().toUByte()
    return if (flags and FLAG_COMMAND == NULL) CommandOrMessage(readMessageContent(flags))
    else CommandOrMessage(readCommandContent(flags))
}

internal suspend fun ByteReadChannel.readCommand(): Command {
    val flags = readByte().toUByte()
    if (flags and FLAG_COMMAND == NULL) invalidFrame("Expected command")
    return readCommandContent(flags)
}

private suspend fun ByteReadChannel.readCommandContent(flags: UByte): Command {
    val shortSize = flags and FLAG_LONG_SIZE == NULL
    val size = if (shortSize) {
        readByte().toUByte().toLong()
    } else {
        readLong().toULong().toLong()
    }

    val commandNameString = readShortString()
    val commandName = CommandName.find(commandNameString)
        ?: invalidFrame("Invalid command name: $commandNameString")

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

private suspend fun ByteReadChannel.readMessageContent(initialFlags: UByte): Message {
    var flags = initialFlags
    val parts = mutableListOf<ByteArray>()

    do {
        if (flags and FLAG_COMMAND != NULL) invalidFrame("Expected message")

        parts.add(readMessagePartContent(flags))

        val hasMore = flags and FLAG_MORE != NULL
        if (hasMore) flags = readByte().toUByte()
    } while (hasMore)

    return Message(parts)
}

private suspend fun ByteReadChannel.readMessagePartContent(flags: UByte): ByteArray {
    val size = if (flags and FLAG_LONG_SIZE == NULL) {
        readByte().toUByte().toLong()
    } else {
        readLong().toULong().toLong()
    }

    // FIXME casting to int for now
    return readBytes(size.toInt())
//    return readMessagePartBody(size.toInt())
}

private suspend fun ByteReadChannel.readMessagePartBody(size: Int): ByteArray {
    val bytes = borrowBuffer()
    readFully(bytes, 0, size)
    return bytes
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
    val propertyName = PropertyName.find(propertyNameString)
        ?: invalidFrame("Can't read property")
    val valueSize = readInt()
    val valueBytes = readBytes(valueSize)
    return propertyName to valueBytes
}

private suspend fun ByteReadChannel.readShortString(): String {
    val size = readByte().toUByte().toInt()
    return readBytes(size).decodeToString()
}

private suspend fun ByteReadChannel.readBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    readFully(bytes)
    return bytes
}
