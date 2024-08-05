/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.selects.*

/**
 * A socket that can receive messages.
 */
public interface ReceiveSocket {
    /**
     * Waits for the next message to become available on the socket. Reads a message immediately if
     * possible. If no messages can be read, it will suspend until the next message is available.
     *
     * @return the message received.
     */
    public suspend fun receive(): Message

    /**
     * Waits for the next message to become available on the socket. Reads a message immediately if
     * possible. If no messages can be read, it will suspend until the next message is available.
     *
     * @return the message received.
     */
    public suspend fun receiveCatching(): SocketResult<Message>

    /**
     * Tries to receive a message from the socket. Reads a message immediately. If no messages can
     * be read, the method returns immediately.
     *
     * @return the message received.
     */
    public fun tryReceive(): SocketResult<Message>

    public val onReceive: SelectClause1<Message>

    /**
     * Underlying kernel receive buffer size in bytes. A value of -1 means leave the OS default
     * unchanged.
     *
     * See [ZMQ_RCVBUF](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var receiveBufferSize: Int

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
    public var receiveHighWaterMark: Int

    /**
     * Sets the timeout receiving messages on the socket. If the value is 0, [receive] will return
     * a rejected promise immediately if there is no message to receive. If the value is -1, it
     * will wait asynchronously until a message is available. For all other values, it will wait
     * for a message for that amount of time before rejecting.
     *
     * See [ZMQ_RCVTIMEO](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var receiveTimeout: Int
}

public suspend inline fun <T> ReceiveSocket.receive(crossinline block: ReadScope.() -> T): T =
    receive().let { it.checkingNoRemainingFrames { block() } }

public suspend inline fun <T> ReceiveSocket.receiveCatching(crossinline block: ReadScope.() -> T): SocketResult<T> =
    receiveCatching().map { it.checkingNoRemainingFrames { block() } }

public inline fun <T> ReceiveSocket.tryReceive(crossinline block: ReadScope.() -> T): SocketResult<T> =
    tryReceive().map { it.checkingNoRemainingFrames { block() } }

public inline fun <T> Message.checkingNoRemainingFrames(crossinline block: ReadScope.() -> T): T {
    val result = block()
    ensureNoRemainingFrames()
    return result
}
