package org.zeromq

interface SendSocket {
    /**
     * Queues a message created from data, so it can be sent.
     *
     * @param data  the data to send.
     */
    suspend fun send(message: Message)

    fun trySend(message: Message): SocketResult<Unit>
}
