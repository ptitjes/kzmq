package org.zeromq

interface Socket {
    /**
     * The 'ZMQ_TYPE option shall retrieve the socket type for the specified
     * 'socket'.  The socket type is specified at socket creation time and
     * cannot be modified afterwards.
     */
    val type: Type

    /**
     * Closes this socket..
     */
    fun close()

    /**
     * Creates an endpoint for accepting connections and binds to it.
     * <p>
     * The endpoint argument is a string consisting of two parts as follows: transport ://address. The
     * transport part specifies the underlying transport protocol to use. The meaning of the address
     * part is specific to the underlying transport protocol selected.
     * </p>
     *
     * @param endpoint the endpoint to bind to
     * @return returns true if bind to the endpoint was successful
     */
    fun bind(endpoint: String): Boolean

    /**
     * Connects the socket to an endpoint and then accepts incoming connections on that endpoint.
     * <p>
     * The endpoint is a string consisting of a transport :// followed by an address. The transport
     * specifies the underlying protocol to use. The address specifies the transport-specific address
     * to connect to.
     * </p>
     *
     * @param endpoint the endpoint to connect to
     * @return returns true if connecting to the endpoint was successful
     */
    fun connect(endpoint: String): Boolean

    /**
     * Disconnecting a socket from an endpoint.
     *
     * @param endpoint the endpoint to disconnect from
     * @return returns true if disconnecting to endpoint was successful
     */
    fun disconnect(endpoint: String): Boolean

    /**
     * The 'ZMQ_SUBSCRIBE' option shall establish a new message filter on a 'ZMQ_SUB' socket.
     * Newly created 'ZMQ_SUB' sockets shall filter out all incoming messages, therefore you
     * should call this option to establish an initial message filter.
     * <p>
     * An empty 'option_value' of length zero shall subscribe to all incoming messages. A
     * non-empty 'option_value' shall subscribe to all messages beginning with the specified
     * prefix. Mutiple filters may be attached to a single 'ZMQ_SUB' socket, in which case a
     * message shall be accepted if it matches at least one filter.
     *
     * @param topic the topic to subscribe to
     * @return true if the option was set, otherwise false
     */
    fun subscribe(topic: ByteArray): Boolean

    /**
     * The 'ZMQ_SUBSCRIBE' option shall establish a new message filter on a 'ZMQ_SUB' socket.
     * Newly created 'ZMQ_SUB' sockets shall filter out all incoming messages, therefore you
     * should call this option to establish an initial message filter.
     * <p>
     * An empty 'option_value' of length zero shall subscribe to all incoming messages. A
     * non-empty 'option_value' shall subscribe to all messages beginning with the specified
     * prefix. Mutiple filters may be attached to a single 'ZMQ_SUB' socket, in which case a
     * message shall be accepted if it matches at least one filter.
     *
     * @param topic the topic to subscribe to
     * @return true if the option was set, otherwise false
     */
    fun subscribe(topic: String): Boolean

    /**
     * The 'ZMQ_UNSUBSCRIBE' option shall remove an existing message filter on a 'ZMQ_SUB'
     * socket. The filter specified must match an existing filter previously established with
     * the 'ZMQ_SUBSCRIBE' option. If the socket has several instances of the same filter
     * attached the 'ZMQ_UNSUBSCRIBE' option shall remove only one instance, leaving the rest in
     * place and functional.
     *
     * @param topic the topic to unsubscribe from
     * @return true if the option was set, otherwise false
     */
    fun unsubscribe(topic: ByteArray): Boolean

    /**
     * The 'ZMQ_UNSUBSCRIBE' option shall remove an existing message filter on a 'ZMQ_SUB'
     * socket. The filter specified must match an existing filter previously established with
     * the 'ZMQ_SUBSCRIBE' option. If the socket has several instances of the same filter
     * attached the 'ZMQ_UNSUBSCRIBE' option shall remove only one instance, leaving the rest in
     * place and functional.
     *
     * @param topic the topic to unsubscribe from
     * @return true if the option was set, otherwise false
     */
    fun unsubscribe(topic: String): Boolean

    /**
     * Queues a message created from data, so it can be sent.
     *
     * @param data  the data to send.
     * @param sendMore whether the message being sent is a multi-part message
     * @return true when it has been queued on the socket and ØMQ has assumed responsibility for the message.
     * This does not indicate that the message has been transmitted to the network.
     */
    suspend fun send(data: ByteArray, sendMore: Boolean = false): Boolean

    /**
     * Receives a message.
     *
     * @return the message received, as an array of bytes.
     */
    suspend fun receive(): ByteArray

    /**
     * Receives a message.
     *
     * @return the message received, as an array of bytes, or null if no message has been received.
     */
    fun receiveOrNull(): ByteArray?
}
