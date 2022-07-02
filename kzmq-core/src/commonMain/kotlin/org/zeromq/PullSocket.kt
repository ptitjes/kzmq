/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

interface PullSocket : Socket, ReceiveSocket {

    /**
     * If set to `true`, a socket shall keep only one message in its inbound/outbound queue: the
     * last message to be received/sent. Ignores any high watermark options. Does not support
     * multi-part messages – in particular, only one part of it is kept in the socket internal
     * queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var conflate: Boolean
}
