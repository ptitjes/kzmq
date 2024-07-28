/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [PUSH][Type.PUSH].
 * Peers must be [PullSocket]s.
 *
 * A [PushSocket] is used by a pipeline node to send messages to downstream pipeline nodes.
 *
 * Messages are distributed round-robin fashion to all connected downstream nodes.
 *
 * When a [PushSocket] enters the mute state due to having reached the high watermark for all downstream nodes,
 * or if there are no downstream nodes at all, then any [send()][SendSocket.send] operations on the socket
 * shall suspend until the mute state ends or at least one downstream node becomes available for sending;
 * messages are not discarded.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>PULL</td></tr>
 * <tr><td>Direction</td><td>Unidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Send only</td></tr>
 * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>Round-robin</td></tr>
 * <tr><td>Action in mute state</td><td>Suspend</td></tr>
 * </table><br/>
 */
public interface PushSocket : Socket, SendSocket {

    /**
     * If set to `true`, a socket shall keep only one message in its inbound/outbound queue: the
     * last message to be received/sent. Ignores any high watermark options. Does not support
     * multi-part messages – in particular, only one part of it is kept in the socket internal
     * queue.
     *
     * See [ZMQ_CONFLATE](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var conflate: Boolean
}
