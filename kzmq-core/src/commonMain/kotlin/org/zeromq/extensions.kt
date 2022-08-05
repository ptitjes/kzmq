/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

public fun ReceiveSocket.consumeAsFlow(): Flow<Message> = flow {
    while (currentCoroutineContext().isActive) emit(receive())
}

public suspend fun Flow<Message>.collectToSocket(socket: SendSocket): Unit = collect {
    socket.send(it)
}

@OptIn(FlowPreview::class)
public fun ReceiveSocket.produceIn(scope: CoroutineScope): ReceiveChannel<Message> =
    consumeAsFlow().produceIn(scope)
