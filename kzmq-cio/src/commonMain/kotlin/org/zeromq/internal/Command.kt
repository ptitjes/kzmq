/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.io.bytestring.*

internal sealed interface Command {
    val name: CommandName
}

internal enum class CommandName {
    READY,
    ERROR,
    SUBSCRIBE,
    CANCEL,
    PING,
    PONG;

    val bytes: ByteString = ByteString(name.encodeToByteArray())

    companion object {
        fun find(string: String): CommandName? {
            return entries.find { it.name.lowercase() == string.lowercase() }
        }
    }
}

internal data class ReadyCommand(val properties: Map<PropertyName, ByteString>) : Command {
    override val name = CommandName.READY

    constructor(vararg properties: Pair<PropertyName, ByteString>) : this(mapOf(*properties))
}

internal data class ErrorCommand(val reason: String) : Command {
    override val name = CommandName.READY
}

internal data class SubscribeCommand(val topic: ByteString) : Command {
    override val name = CommandName.SUBSCRIBE
}

internal data class CancelCommand(val topic: ByteString) : Command {
    override val name = CommandName.CANCEL
}

internal data class PingCommand(val ttl: UShort, val context: ByteString) : Command {
    override val name = CommandName.PING
}

internal data class PongCommand(val context: ByteString) : Command {
    override val name = CommandName.PONG
}

internal enum class PropertyName(val propertyName: String) {
    SOCKET_TYPE("Socket-Type"),
    IDENTITY("Identity"),
    RESOURCE("Resource");

    val bytes: ByteString = ByteString(propertyName.encodeToByteArray())

    companion object {
        fun find(string: String): PropertyName? {
            return entries.find { it.propertyName.lowercase() == string.lowercase() }
        }
    }
}
