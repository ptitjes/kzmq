/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public interface RouterSocket : Socket, SendSocket, ReceiveSocket {

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
     * A value of `false` is the default and discards the message silently when it cannot be routed
     * or the peer's high watermark is reached. A value of `true` causes [send] to fail if it
     * cannot be routed, or wait asynchronously if the high watermark is reached.
     *
     * See [ZMQ_ROUTER_MANDATORY](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var mandatory: Boolean

    /**
     * If two clients use the same identity when connecting to a [RouterSocket], the results shall
     * depend on this option. If it set to `false` (default), the [RouterSocket] shall reject
     * clients trying to connect with an already-used identity. If it is set to `true`, the
     * [RouterSocket] shall hand-over the connection to the new client and disconnect the existing
     * one.
     *
     * See [ZMQ_ROUTER_HANDOVER](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var handover: Boolean
}
