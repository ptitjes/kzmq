package org.zeromq

interface SendSocket {
    /**
     * Queues a message created from data, so it can be sent.
     *
     * @param message the message to send.
     */
    suspend fun send(message: Message)

    suspend fun sendCatching(message: Message): SocketResult<Unit>

    fun trySend(message: Message): SocketResult<Unit>
}
