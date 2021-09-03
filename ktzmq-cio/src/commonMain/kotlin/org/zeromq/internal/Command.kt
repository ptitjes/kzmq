package org.zeromq.internal

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

    val bytes: ByteArray = name.encodeToByteArray()

    companion object {
        fun find(string: String): CommandName? {
            return values().find { it.name.lowercase() == string.lowercase() }
        }
    }
}

internal data class ReadyCommand(val properties: Map<PropertyName, ByteArray>) : Command {
    override val name = CommandName.READY

    constructor(vararg properties: Pair<PropertyName, ByteArray>) : this(mapOf(*properties))
}

internal data class ErrorCommand(val reason: String) : Command {
    override val name = CommandName.READY
}

internal data class SubscribeCommand(val topic: ByteArray) : Command {
    override val name = CommandName.SUBSCRIBE
}

internal data class CancelCommand(val topic: ByteArray) : Command {
    override val name = CommandName.CANCEL
}

internal data class PingCommand(val ttl: UShort, val context: ByteArray) : Command {
    override val name = CommandName.PING
}

internal data class PongCommand(val context: ByteArray) : Command {
    override val name = CommandName.PONG
}

internal enum class PropertyName(val propertyName: String) {
    SOCKET_TYPE("Socket-Type"),
    IDENTITY("Identity"),
    RESOURCE("Resource");

    val bytes: ByteArray = propertyName.encodeToByteArray()

    companion object {
        fun find(string: String): PropertyName? {
            return values().find { it.propertyName.lowercase() == string.lowercase() }
        }
    }
}
