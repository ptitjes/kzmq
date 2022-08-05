/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public interface RequestSocket : Socket, SendSocket, ReceiveSocket {

    /**
     * The identity of this socket when connecting to a [RouterSocket].
     *
     * See [ZMQ_ROUTING_ID](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var routingId: ByteArray?

    /**
     * When set to `true`, the socket will automatically send an empty message when a new
     * connection is made or accepted. You may set this on sockets connected to a [RouterSocket].
     * The application must filter such empty messages. This option provides the [RouterSocket]
     * with an event signaling the arrival of a new peer.
     *
     * Warning: Do not set this option on a socket that talks to any other socket type except
     * [RouterSocket], because the results are undefined.
     *
     * See [ZMQ_PROBE_ROUTER](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var probeRouter: Boolean

    /**
     * The default behaviour of [RequestSocket] is to rely on the ordering of messages to match
     * requests and responses and that is usually sufficient. When this option is set to `true` the
     * socket will prefix outgoing messages with an extra frame containing a request id. That means
     * the full message is `[<request id>, null, user frames...]`. The [RequestSocket] will discard
     * all incoming messages that don't begin with these two frames.
     *
     * See [ZMQ_REQ_CORRELATE](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var correlate: Boolean

    /**
     * By default, a [RequestSocket] does not allow initiating a new request until the reply to the
     * previous one has been received. When set to `true`, sending another message is allowed and
     * previous replies will be discarded. The request-reply state machine is reset and a new
     * request is sent to the next available peer.
     *
     * Note: If set to `true`, also enable [correlate] to ensure correct matching of requests and
     * replies. Otherwise, a late reply to an aborted request can be reported as the reply to the
     * superseding request.
     *
     * See [ZMQ_REQ_RELAXED](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var relaxed: Boolean
}
