/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*

/**
 * A ZeroMQ socket of type [REP][Type.ROUTER].
 * Peers must be [RequestSocket]s or [DealerSocket]s.
 *
 * A [RouterSocket] creates and consumes request-reply routing envelopes.
 * It is the only socket type that lets you route messages to specific connections if you know their identities.
 *
 * A [RouterSocket] is an advanced socket type used for extending request/reply sockets.
 *
 * When receiving messages a [RouterSocket] shall prepend a message frame containing the identity
 * of the originating peer to the message before passing it to the application.
 *
 * Messages received are fair-queued from all connected peers.
 *
 * When sending messages a [RouterSocket] shall remove the first frame of the message
 * and use it to determine the identity of the peer the message shall be routed to.
 * If the peer does not exist anymore the message shall be silently discarded by default,
 * unless [RouterSocket.mandatory] is set to `true`.
 *
 * When a [RouterSocket] enters the mute state due to having reached the high watermark for all peers,
 * then any messages sent to the socket shall be dropped until the mute state ends.
 *
 * Likewise, any messages routed to a peer for which the individual high watermark has been reached
 * shall also be dropped, unless [RouterSocket.mandatory] is set to true.
 *
 * When a [RequestSocket] is connected to a [RouterSocket], in addition to the identity of the originating peer
 * each message received shall contain an empty delimiter message frame.
 *
 * Hence, the entire structure of each received message as seen by the application becomes:
 * one or more identity frames, an empty delimiter frame, one or more body frames.
 *
 * When sending replies to a [RequestSocket] the application must include the empty delimiter frame.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>DEALER, REQ, ROUTER</td></tr>
 * <tr><td>Direction</td><td>Bidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Unrestricted</td></tr>
 * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>See text</td></tr>
 * <tr><td>Action in mute state</td><td>Drop (See text)</td></tr>
 * </table><br/>
 */
public interface RouterSocket : Socket, SendSocket, ReceiveSocket {

    /**
     * The identity of this socket when connecting to a [RouterSocket].
     *
     * See [ZMQ_ROUTING_ID](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var routingId: ByteString?

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
