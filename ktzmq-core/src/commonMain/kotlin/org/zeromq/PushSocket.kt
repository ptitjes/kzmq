package org.zeromq

interface PushSocket : Socket, SendSocket {
    override val type: Type get() = Type.PUSH

    /**
     * If set to `true`, a socket shall keep only one message in its inbound/outbound queue: the
     * last message to be received/sent. Ignores any high watermark options. Does not support
     * multi-part messages – in particular, only one part of it is kept in the socket internal
     * queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master#zmq-getsockopt)
     */
    var conflate: Boolean
}
