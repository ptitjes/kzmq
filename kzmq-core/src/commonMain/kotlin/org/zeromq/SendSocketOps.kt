/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.flow.*

public suspend fun SendSocket.send(sender: WriteScope.() -> Unit) {
    send(buildMessage { sender() })
}

public suspend fun SendSocket.sendCatching(sender: WriteScope.() -> Unit): SocketResult<Unit> =
    sendCatching(buildMessage { sender() })

public fun SendSocket.trySend(sender: WriteScope.() -> Unit): SocketResult<Unit> =
    trySend(buildMessage { sender() })

/**
 * Experimental API. Implementation is subject to change.
 */
public suspend fun Flow<Message>.collectToSocket(socket: SendSocket): Unit = collect {
    socket.send(it)
}
