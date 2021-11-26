/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.selects.*

interface ReceiveSocket {
    /**
     * Waits for the next message to become available on the socket. Reads a message immediately if
     * possible. If no messages can be read, it will suspend until the next message is available.
     *
     * @return the message received.
     */
    suspend fun receive(): Message

    /**
     * Waits for the next message to become available on the socket. Reads a message immediately if
     * possible. If no messages can be read, it will suspend until the next message is available.
     *
     * @return the message received.
     */
    suspend fun receiveCatching(): SocketResult<Message>

    /**
     * Tries to receive a message from the socket. Reads a message immediately. If no messages can
     * be read, the method returns immediately.
     *
     * @return the message received.
     */
    fun tryReceive(): SocketResult<Message>

    val onReceive: SelectClause1<Message>

    /**
     * Returns a new iterator to receive messages from this socket using a for loop. Iteration
     * completes normally when the socket is closed or throws if an error occurs.
     *
     * @return the message received.
     */
    operator fun iterator(): SocketIterator

    /**
     * Underlying kernel receive buffer size in bytes. A value of -1 means leave the OS default
     * unchanged.
     *
     * See [ZMQ_RCVBUF](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var receiveBufferSize: Int

    /**
     * The high watermark is a hard limit on the maximum number of incoming messages ØMQ shall
     * queue in memory for any single peer that the specified socket is communicating with. A value
     * of zero means no limit.
     *
     * If this limit has been reached the socket shall enter an exceptional state and depending on
     * the socket type, ØMQ shall take appropriate action such as blocking or dropping sent
     * messages.
     *
     * See [ZMQ_RCVHWM](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var receiveHighWaterMark: Int

    /**
     * Sets the timeout receiving messages on the socket. If the value is 0, [receive] will return
     * a rejected promise immediately if there is no message to receive. If the value is -1, it
     * will wait asynchronously until a message is available. For all other values, it will wait
     * for a message for that amount of time before rejecting.
     *
     * See [ZMQ_RCVTIMEO](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var receiveTimeout: Int
}
