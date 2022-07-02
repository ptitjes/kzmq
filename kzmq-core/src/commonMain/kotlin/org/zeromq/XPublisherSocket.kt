/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

interface XPublisherSocket : Socket, SendSocket {

    /**
     * Sets the socket behaviour to return an error if the high watermark is reached and the
     * message could not be sent. The default is to drop the message silently when the peer high
     * watermark is reached.
     *
     * See [ZMQ_XPUB_NODROP](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var noDrop: Boolean

    /**
     * Causes messages to be sent to all connected sockets except those subscribed to a prefix that
     * matches the message.
     *
     * See [ZMQ_INVERT_MATCHING](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var invertMatching: Boolean

    /**
     * Sets the socket subscription handling mode to manual/automatic. A value of true will change
     * the subscription requests handling to manual.
     *
     * See [ZMQ_XPUB_MANUAL](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var manual: Boolean

    /**
     * Sets a welcome message that will be received by subscriber when connecting. Subscriber must
     * subscribe to the welcome message before connecting. For welcome messages to work well, poll
     * on incoming subscription messages on the XPublisher socket and handle them.
     *
     * See [ZMQ_XPUB_WELCOME_MSG](http://api.zeromq.org/master:zmq-getsockopt)
     */
    var welcomeMessage: String?
}
