package org.zeromq.wire

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

internal enum class PropertyName(propertyName: String) {
    SOCKET_TYPE("Socket-Type"),
    IDENTITY("Identity"),
    RESOURCE("Resource");

    val bytes: ByteArray = propertyName.encodeToByteArray()

    companion object {
        fun find(string: String): PropertyName? {
            return values().find { it.name.lowercase() == string.lowercase() }
        }
    }
}
