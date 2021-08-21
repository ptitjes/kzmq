package org.zeromq

interface ReplySocket : Socket, SendSocket, ReceiveSocket {
    override val type: Type get() = Type.REP

    /**
     * The identity of this socket when connecting to a [RouterSocket].
     *
     * See [ZMQ_ROUTING_ID](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var routingId: String?
}
