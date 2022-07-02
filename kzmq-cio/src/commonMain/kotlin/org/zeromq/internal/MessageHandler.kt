/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

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
