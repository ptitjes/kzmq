package org.zeromq

interface SubscriberSocket : Socket, ReceiveSocket {
    override val type: Type get() = Type.SUB

    /**
     * The 'ZMQ_SUBSCRIBE' option shall establish a new message filter on a 'ZMQ_SUB' socket.
     * Newly created 'ZMQ_SUB' sockets shall filter out all incoming messages, therefore you
     * should call this option to establish an initial message filter.
     * <p>
     * An empty 'option_value' of length zero shall subscribe to all incoming messages. A
     * non-empty 'option_value' shall subscribe to all messages beginning with the specified
     * prefix. Multiple filters may be attached to a single 'ZMQ_SUB' socket, in which case a
     * message shall be accepted if it matches at least one filter.
     *
     * @param topic the topic to subscribe to
     */
    fun subscribe(topic: ByteArray)

    /**
     * The 'ZMQ_SUBSCRIBE' option shall establish a new message filter on a 'ZMQ_SUB' socket.
     * Newly created 'ZMQ_SUB' sockets shall filter out all incoming messages, therefore you
     * should call this option to establish an initial message filter.
     * <p>
     * An empty 'option_value' of length zero shall subscribe to all incoming messages. A
     * non-empty 'option_value' shall subscribe to all messages beginning with the specified
     * prefix. Multiple filters may be attached to a single 'ZMQ_SUB' socket, in which case a
     * message shall be accepted if it matches at least one filter.
     *
     * @param topic the topic to subscribe to
     */
    fun subscribe(topic: String)

    /**
     * The 'ZMQ_UNSUBSCRIBE' option shall remove an existing message filter on a 'ZMQ_SUB'
     * socket. The filter specified must match an existing filter previously established with
     * the 'ZMQ_SUBSCRIBE' option. If the socket has several instances of the same filter
     * attached the 'ZMQ_UNSUBSCRIBE' option shall remove only one instance, leaving the rest in
     * place and functional.
     *
     * @param topic the topic to unsubscribe from
     */
    fun unsubscribe(topic: ByteArray)

    /**
     * The 'ZMQ_UNSUBSCRIBE' option shall remove an existing message filter on a 'ZMQ_SUB'
     * socket. The filter specified must match an existing filter previously established with
     * the 'ZMQ_SUBSCRIBE' option. If the socket has several instances of the same filter
     * attached the 'ZMQ_UNSUBSCRIBE' option shall remove only one instance, leaving the rest in
     * place and functional.
     *
     * @param topic the topic to unsubscribe from
     */
    fun unsubscribe(topic: String)
}
