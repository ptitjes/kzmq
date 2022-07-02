/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

interface PublisherSocket : Socket, SendSocket {

    /**
     * If set to `true`, a socket shall keep only one message in its inbound/outbound queue: the
     * last message to be received/sent. Ignores any high watermark options. Does not support
     * multi-part messages – in particular, only one part of it is kept in the socket internal
     * queue.
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

    /**
     * Sets the socket behaviour to return an error if the high watermark is reached and the
     * message could not be sent. The default is to drop the message silently when the peer high
     * watermark is reached.
     *
     * See [ZMQ_XPUB_NODROP](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var noDrop: Boolean
}
