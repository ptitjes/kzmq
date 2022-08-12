/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [PUB][Type.PUB].
 * Peers must be [SubscriberSocket]s or [XSubscriberSocket]s.
 *
 * A [PublisherSocket] is used by a publisher to distribute data.
 *
 * Messages sent are distributed in a fan-out fashion to all connected peers.
 *
 * When a [PublisherSocket] enters the mute state due to having reached the high watermark for a subscriber,
 * then any messages that would be sent to the subscriber in question shall instead be dropped until the mute
 * state ends.
 *
 * The [send][SendSocket.send] methods shall never block for this socket type.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>SUB, XSUB</td></tr>
 * <tr><td>Direction</td><td>Unidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Send only</td></tr>
 * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>Fan-out</td></tr>
 * <tr><td>Action in mute state</td><td>Drop</td></tr>
 * </table><br/>
 */
public interface PublisherSocket : Socket, SendSocket {

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
     * Causes messages to be sent to all connected sockets except those subscribed to a prefix that
     * matches the message.
     *
     * See [ZMQ_INVERT_MATCHING](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var invertMatching: Boolean

    /**
     * Sets the socket behaviour to return an error if the high watermark is reached and the
     * message could not be sent. The default is to drop the message silently when the peer high
     * watermark is reached.
     *
     * See [ZMQ_XPUB_NODROP](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var noDrop: Boolean
}
