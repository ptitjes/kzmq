/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [DEALER][Type.DEALER].
 * Peers must be [ReplySocket]s or [RouterSocket]s.
 *
 * A [DealerSocket] does load-balancing on outputs and fair-queuing on inputs with no other semantics.
 * It is the only socket type that lets you shuffle messages out to N nodes and shuffle the replies back,
 * in a raw bidirectional asynchronous pattern.
 *
 * A [DealerSocket] is an advanced pattern used for extending request/reply sockets.
 *
 * Each message sent is distributed in a round-robin fashion among all connected peers,
 * and each message received is fair-queued from all connected peers.
 *
 * When a [DealerSocket] enters the mute state due to having reached the high watermark for all peers,
 * or if there are no peers at all, then any [send()][SendSocket.send] operations on the socket shall suspend
 * until the mute state ends or at least one peer becomes available for sending; messages are not discarded.
 *
 * When a [DealerSocket] is connected to a [ReplySocket] each message sent must consist of
 * an empty message frame, the delimiter, followed by one or more body parts.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>ROUTER, REP, DEALER</td></tr>
 * <tr><td>Direction</td><td>Bidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Unrestricted</td></tr>
 * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>Round-robin</td></tr>
 * <tr><td>Action in mute state</td><td>Suspend</td></tr>
 * </table><br/>
 */
public interface DealerSocket : Socket, SendSocket, ReceiveSocket {

    /**
     * If set to `true`, a socket shall keep only one message in its inbound/outbound queue: the
     * last message to be received/sent. Ignores any high watermark options. Does not support
     * multi-part messages – in particular, only one part of it is kept in the socket internal
     * queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var conflate: Boolean

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
}
