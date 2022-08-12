/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [PULL][Type.PULL].
 * Peers must be [PushSocket]s.
 *
 * A [PullSocket] is used by a pipeline node to receive messages from upstream pipeline nodes.
 *
 * Messages are fair-queued from all connected upstream nodes.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>PUSH</td></tr>
 * <tr><td>Direction</td><td>Unidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Receive only</td></tr>
 * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
 * <tr><td>Action in mute state</td><td>Suspend</td></tr>
 * </table><br/>
 */
public interface PullSocket : Socket, ReceiveSocket {

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
