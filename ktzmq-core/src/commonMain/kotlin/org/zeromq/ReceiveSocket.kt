package org.zeromq

interface ReceiveSocket {
    /**
     * Receives a message.
     *
     * @return the message received.
     */
    suspend fun receive(): Message

    suspend fun receiveCatching(): SocketResult<Message>

    fun tryReceive(): SocketResult<Message>

    operator fun iterator(): SocketIterator
}
