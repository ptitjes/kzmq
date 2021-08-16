package org.zeromq

interface ReceiveSocket {
    /**
     * Receives a message.
     *
     * @return the message received, as an array of bytes.
     */
    suspend fun receive(): Message

    fun tryReceive(): SocketResult<Message>

    operator fun iterator(): SocketIterator
}

