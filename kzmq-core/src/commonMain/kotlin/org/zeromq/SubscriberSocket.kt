/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

interface SubscriberSocket : Socket, ReceiveSocket {

    /**
     * Establishes a new message filter. Newly created [SubscriberSocket] sockets will filter out
     * all incoming messages. Call this method to subscribe to all incoming messages.
     *
     * Multiple filters may be attached to a single socket, in which case a message shall be
     * accepted if it matches at least one filter. Subscribing without any filters shall subscribe
     * to all incoming messages.
     */
    suspend fun subscribe()

    /**
     * Establishes a new message filter. Newly created [SubscriberSocket] sockets will filter out
     * all incoming messages. Call this method to subscribe for messages beginning with the given
     * prefix.
     *
     * Multiple filters may be attached to a single socket, in which case a message shall be
     * accepted if it matches at least one filter. Subscribing without any filters shall subscribe
     * to all incoming messages.
     *
     * @param topics the topics to subscribe to
     */
    suspend fun subscribe(vararg topics: ByteArray)

    /**
     * Establishes a new message filter. Newly created [SubscriberSocket] sockets will filter out
     * all incoming messages. Call this method to subscribe for messages beginning with the given
     * prefix.
     *
     * Multiple filters may be attached to a single socket, in which case a message shall be
     * accepted if it matches at least one filter. Subscribing without any filters shall subscribe
     * to all incoming messages.
     *
     * @param topics the topics to subscribe to
     */
    suspend fun subscribe(vararg topics: String)

    /**
     * Removes the "subscribe all" message filter which was previously established with
     * [subscribe].
     *
     * Unsubscribing without any filters shall unsubscribe from the "subscribe all" filter that is
     * added by calling [subscribe] without arguments.
     */
    suspend fun unsubscribe()

    /**
     * Removes the specified existing message filter previously established with [subscribe].
     *
     * Unsubscribing without any filters shall unsubscribe from the "subscribe all" filter that is
     * added by calling [subscribe] without arguments.
     *
     * @param topics the topics to unsubscribe from
     */
    suspend fun unsubscribe(vararg topics: ByteArray)

    /**
     * Removes the specified existing message filter previously established with [subscribe].
     *
     * Unsubscribing without any filters shall unsubscribe from the "subscribe all" filter that is
     * added by calling [subscribe] without arguments.
     *
     * @param topics the topics to unsubscribe from
     */
    suspend fun unsubscribe(vararg topics: String)

    /**
     * If set to true, a socket shall keep only one message in its inbound/outbound queue: the last
     * message to be received/sent. Ignores any high watermark options. Does not support multi-part
     * messages – in particular, only one part of it is kept in the socket internal queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var conflate: Boolean

    /**
     * Causes messages to be sent to all connected sockets except those subscribed to a prefix that
     * matches the message.
     *
     * See [ZMQ_INVERT_MATCHING](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var invertMatching: Boolean
}
