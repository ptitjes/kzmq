package org.zeromq

interface SubscriberSocket : Socket, ReceiveSocket {

    /**
     * Establish a new message filter. Newly created [SubscriberSocket] sockets will filter out all
     * incoming messages. Call this method to subscribe for messages beginning with the given
     * prefix.
     *
     * Multiple filters may be attached to a single socket, in which case a message shall be
     * accepted if it matches at least one filter. Subscribing without any filters shall subscribe
     * to all incoming messages.
     *
     * @param topics the topics to subscribe to
     */
    fun subscribe(vararg topics: ByteArray)

    /**
     * Establish a new message filter. Newly created [SubscriberSocket] sockets will filter out all
     * incoming messages. Call this method to subscribe for messages beginning with the given
     * prefix.
     *
     * Multiple filters may be attached to a single socket, in which case a message shall be
     * accepted if it matches at least one filter. Subscribing without any filters shall subscribe
     * to all incoming messages.
     *
     * @param topics the topics to subscribe to
     */
    fun subscribe(vararg topics: String)

    /**
     * Remove an existing message filter which was previously established with [subscribe]. Stops
     * receiving messages with the given prefix.
     *
     * Unsubscribing without any filters shall unsubscribe from the "subscribe all" filter that is
     * added by calling [subscribe] without arguments.
     *
     * @param topics the topics to unsubscribe from
     */
    fun unsubscribe(vararg topics: ByteArray)

    /**
     * Remove an existing message filter which was previously established with [subscribe]. Stops
     * receiving messages with the given prefix.
     *
     * Unsubscribing without any filters shall unsubscribe from the "subscribe all" filter that is
     * added by calling [subscribe] without arguments.
     *
     * @param topics the topics to unsubscribe from
     */
    fun unsubscribe(vararg topics: String)

    /**
     * If set to true, a socket shall keep only one message in its inbound/outbound queue: the last
     * message to be received/sent. Ignores any high watermark options. Does not support multi-part
     * messages – in particular, only one part of it is kept in the socket internal queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var conflate: Boolean

    /**
     * Causes messages to be sent to all connected sockets except those subscribed to a prefix that
     * matches the message.
     *
     * See [ZMQ_INVERT_MATCHING](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var invertMatching: Boolean
}
