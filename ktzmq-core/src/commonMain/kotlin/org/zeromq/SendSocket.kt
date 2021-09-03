package org.zeromq

interface SendSocket {
    /**
     * Sends a message on the socket. Queues the message immediately if possible. If the message
     * cannot be queued because the high watermark has been reached, it will suspend until the
     * message was queued successfully.
     *
     * @param message the message to send.
     */
    suspend fun send(message: Message)

    /**
     * Sends a message on the socket. Queues the message immediately if possible. If the message
     * cannot be queued because the high watermark has been reached, it will suspend until the
     * message was queued successfully.
     *
     * @param message the message to send.
     * @return
     */
    suspend fun sendCatching(message: Message): SocketResult<Unit>

    /**
     * Tries to send a message on the socket. Queues the message immediately. If the message cannot
     * be queued because the high watermark has been reached, the method returns immediately.
     *
     * @param message the message to send.
     * @return
     */
    fun trySend(message: Message): SocketResult<Unit>

    /**
     * Sets the time-to-live field in every multicast packet sent from this socket. The default is
     * 1 which means that the multicast packets don't leave the local network.
     *
     * See [ZMQ_MULTICAST_HOPS](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var multicastHops: Int

    /**
     * Underlying kernel transmit buffer size in bytes. A value of -1 means leave the OS default
     * unchanged.
     *
     * See [ZMQ_SNDBUF](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var sendBufferSize: Int

    /**
     * The high watermark is a hard limit on the maximum number of outgoing messages ØMQ shall
     * queue in memory for any single peer that the specified socket is communicating with. A value
     * of zero means no limit.
     *
     * If this limit has been reached the socket shall enter an exceptional state and depending on
     * the socket type, ØMQ shall take appropriate action such as blocking or dropping sent
     * messages.
     *
     * See [ZMQ_SNDHWM](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var sendHighWaterMark: Int

    /**
     * Sets the timeout for sending messages on the socket. If the value is 0, [send] will return a
     * rejected promise immediately if the message cannot be sent. If the value is -1, it will wait
     * asynchronously until the message is sent. For all other values, it will try to send the
     * message for that amount of time before rejecting.
     *
     * See [ZMQ_SNDTIMEO](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var sendTimeout: Int
}