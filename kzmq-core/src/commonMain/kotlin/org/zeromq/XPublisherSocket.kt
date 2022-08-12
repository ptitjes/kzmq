/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [XPUB][Type.XPUB].
 *
 * The behavior of an [XPublisherSocket] is the same as a [PublisherSocket],
 * except that you can receive subscription/unsubscription messages from the peers.
 *
 * Subscription messages contain a unique frame starting with a '1' byte.
 * Subscription cancellation messages contain a unique frame starting with a '0' byte.
 * Other messages are distributed as is.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>SUB, XSUB</td></tr>
 * <tr><td>Direction</td><td>Unidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Send messages, receive subscriptions</td></tr>
 * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>Fan-out</td></tr>
 * <tr><td>Action in mute state</td><td>Drop</td></tr>
 * </table><br/>
 */
public interface XPublisherSocket : Socket, SendSocket, ReceiveSocket {

    /**
     * Sets the socket behaviour to return an error if the high watermark is reached and the
     * message could not be sent. The default is to drop the message silently when the peer high
     * watermark is reached.
     *
     * See [ZMQ_XPUB_NODROP](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var noDrop: Boolean

    /**
     * Causes messages to be sent to all connected sockets except those subscribed to a prefix that
     * matches the message.
     *
     * See [ZMQ_INVERT_MATCHING](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var invertMatching: Boolean

    /**
     * Sets the socket subscription handling mode to manual/automatic. A value of true will change
     * the subscription requests handling to manual.
     *
     * See [ZMQ_XPUB_MANUAL](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var manual: Boolean

    /**
     * Sets a welcome message that will be received by subscriber when connecting. Subscriber must
     * subscribe to the welcome message before connecting. For welcome messages to work well, poll
     * on incoming subscription messages on the XPublisher socket and handle them.
     *
     * See [ZMQ_XPUB_WELCOME_MSG](http://api.zeromq.org/master:zmq-getsockopt)
     */
    public var welcomeMessage: String?
}
