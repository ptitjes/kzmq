/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

/**
 * Experimental API. Implementation is subject to change.
 */
public fun ReceiveSocket.consumeAsFlow(): Flow<Message> = flow {
    while (currentCoroutineContext().isActive) emit(receive())
}

/**
 * Experimental API. Implementation is subject to change.
 */
public suspend fun Flow<Message>.collectToSocket(socket: SendSocket): Unit = collect {
    socket.send(it)
}

/**
 * Experimental API. Implementation is subject to change.
 */
@OptIn(FlowPreview::class)
public fun ReceiveSocket.produceIn(scope: CoroutineScope): ReceiveChannel<Message> =
    consumeAsFlow().produceIn(scope)
