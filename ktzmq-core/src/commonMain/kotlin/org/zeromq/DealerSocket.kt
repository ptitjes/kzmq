package org.zeromq

interface DealerSocket : Socket, SendSocket, ReceiveSocket {

    /**
     * If set to `true`, a socket shall keep only one message in its inbound/outbound queue: the
     * last message to be received/sent. Ignores any high watermark options. Does not support
     * multi-part messages – in particular, only one part of it is kept in the socket internal
     * queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var conflate: Boolean

    /**
     * The identity of this socket when connecting to a [RouterSocket].
     *
     * See [ZMQ_ROUTING_ID](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var routingId: String?

    /**
     * When set to `true`, the socket will automatically send an empty message when a new
     * connection is made or accepted. You may set this on sockets connected to a [RouterSocket].
     * The application must filter such empty messages. This option provides the [RouterSocket]
     * with an event signaling the arrival of a new peer.
     *
     * Warning: Do not set this option on a socket that talks to any other socket type except
     * [RouterSocket], because the results are undefined.
     *
     * See [ZMQ_PROBE_ROUTER](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var probeRouter: Boolean
}
