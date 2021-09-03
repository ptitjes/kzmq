package org.zeromq.internal

internal interface MessageHandler {
    suspend fun handleIncoming(
        incoming: CommandOrMessage,
        receive: suspend (incoming: CommandOrMessage) -> Unit
    )

    suspend fun handleOutgoing(
        outgoing: CommandOrMessage,
        send: suspend (outgoing: CommandOrMessage) -> Unit
    )
}

internal object NoopMessageHandler : MessageHandler {
    override suspend fun handleIncoming(
        incoming: CommandOrMessage,
        receive: suspend (incoming: CommandOrMessage) -> Unit
    ) = receive(incoming)

    override suspend fun handleOutgoing(
        outgoing: CommandOrMessage,
        send: suspend (outgoing: CommandOrMessage) -> Unit
    ) = send(outgoing)
}
