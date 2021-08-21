package org.zeromq.wire

import org.zeromq.Message

internal sealed interface CommandOrMessage {

    val isCommand: Boolean get() = this is CommandCase
    val isMessage: Boolean get() = this is MessageCase

    fun commandOrThrow(): Command = when (this) {
        is CommandCase -> this.command
        is MessageCase -> error("Not a message")
    }

    fun messageOrThrow(): Message = when (this) {
        is MessageCase -> this.message
        is CommandCase -> error("Not a message")
    }

    private data class CommandCase(val command: Command) : CommandOrMessage
    private data class MessageCase(val message: Message) : CommandOrMessage

    companion object {
        operator fun invoke(command: Command): CommandOrMessage = CommandCase(command)
        operator fun invoke(message: Message): CommandOrMessage = MessageCase(message)
    }
}
