/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket factory.
 *
 * @see Context
 */
public interface SocketFactory {
    /**
     * Creates a [PairSocket].
     */
    public fun createPair(): PairSocket

    /**
     * Creates a [PublisherSocket].
     */
    public fun createPublisher(): PublisherSocket

    /**
     * Creates a [SubscriberSocket].
     */
    public fun createSubscriber(): SubscriberSocket

    /**
     * Creates a [XPublisherSocket].
     */
    public fun createXPublisher(): XPublisherSocket

    /**
     * Creates a [XSubscriberSocket].
     */
    public fun createXSubscriber(): XSubscriberSocket

    /**
     * Creates a [PushSocket].
     */
    public fun createPush(): PushSocket

    /**
     * Creates a [PullSocket].
     */
    public fun createPull(): PullSocket

    /**
     * Creates a [RequestSocket].
     */
    public fun createRequest(): RequestSocket

    /**
     * Creates a [ReplySocket].
     */
    public fun createReply(): ReplySocket

    /**
     * Creates a [DealerSocket].
     */
    public fun createDealer(): DealerSocket

    /**
     * Creates a [RouterSocket].
     */
    public fun createRouter(): RouterSocket
}
