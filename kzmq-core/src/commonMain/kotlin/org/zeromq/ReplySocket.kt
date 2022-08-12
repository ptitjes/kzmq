/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [REP][Type.REP].
 * Peers must be [RequestSocket]s or [DealerSocket]s.
 *
 * A [ReplySocket] is used by a service to receive requests from and send replies to a client.
 *
 * This socket type only allows an alternating sequence of [send()][SendSocket.send] and subsequent
 * [receive()][SendSocket.receive] calls.
 *
 * Each request received is fair-queued from all connected peers,
 * and each reply sent is routed to the peer that issued the last request.
 *
 * If the original requester does not exist any more the reply is silently discarded.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>REQ, DEALER</td></tr>
 * <tr><td>Direction</td><td>Bidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Receive, Send, Receive, Send, ...</td></tr>
 * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>Last peer</td></tr>
 * </table><br/>
 */
public interface ReplySocket : Socket, SendSocket, ReceiveSocket {

    /**
     * The identity of this socket when connecting to a [RouterSocket].
     *
     * See [ZMQ_ROUTING_ID](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var routingId: ByteArray?
}
