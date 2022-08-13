/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal const val TRACE = false

internal class JeroMQEngine private constructor(
    private val underlying: ZContext,
) : Engine {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads))

    override fun close() {
        underlying.close()
    }

    override fun createPair(): PairSocket = wrapping { JeroMQPairSocket(::newSocket) }
    override fun createPublisher(): PublisherSocket = wrapping { JeroMQPublisherSocket(::newSocket) }
    override fun createSubscriber(): SubscriberSocket = wrapping { JeroMQSubscriberSocket(::newSocket) }
    override fun createXPublisher(): XPublisherSocket = wrapping { JeroMQXPublisherSocket(::newSocket) }
    override fun createXSubscriber(): XSubscriberSocket = wrapping { JeroMQXSubscriberSocket(::newSocket) }
    override fun createPush(): PushSocket = wrapping { JeroMQPushSocket(::newSocket) }
    override fun createPull(): PullSocket = wrapping { JeroMQPullSocket(::newSocket) }
    override fun createRequest(): RequestSocket = wrapping { JeroMQRequestSocket(::newSocket) }
    override fun createReply(): ReplySocket = wrapping { JeroMQReplySocket(::newSocket) }
    override fun createDealer(): DealerSocket = wrapping { JeroMQDealerSocket(::newSocket) }
    override fun createRouter(): RouterSocket = wrapping { JeroMQRouterSocket(::newSocket) }

    private fun newSocket(type: SocketType): ZMQ.Socket = underlying.createSocket(type)
}
