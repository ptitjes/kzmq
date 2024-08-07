/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
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
@OptIn(FlowPreview::class)
public fun ReceiveSocket.produceIn(scope: CoroutineScope): ReceiveChannel<Message> =
    consumeAsFlow().produceIn(scope)

/**
 * Returns a new iterator to receive messages from this socket using a for loop. Iteration
 * completes normally when the socket is closed or throws if an error occurs.
 *
 * @return the message received.
 */
public operator fun ReceiveSocket.iterator(): SocketIterator = SocketIteratorImpl(this)

internal class SocketIteratorImpl(private val socket: ReceiveSocket) : SocketIterator {
    private var nextMessage: Message? = null

    override suspend fun hasNext(): Boolean {
        if (nextMessage == null) nextMessage = socket.receive()
        return nextMessage != null
    }

    override fun next(): Message {
        val message = nextMessage ?: error("No next message")
        nextMessage = null
        return message
    }
}
