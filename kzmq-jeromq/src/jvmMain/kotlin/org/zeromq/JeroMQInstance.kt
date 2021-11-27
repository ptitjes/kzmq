/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal const val TRACE = true

internal class JeroMQInstance private constructor(
    private val underlying: ZContext,
) : EngineInstance {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads))

    override fun close() {
        underlying.close()
    }

    override fun createPublisher(): PublisherSocket = wrapping {
        JeroMQPublisherSocket(newSocket(SocketType.PUB))
    }

    override fun createSubscriber(): SubscriberSocket = wrapping {
        JeroMQSubscriberSocket(newSocket(SocketType.SUB))
    }

    override fun createPair(): PairSocket {
        TODO("Not yet implemented")
    }

    override fun createXPublisher(): XPublisherSocket {
        TODO("Not yet implemented")
    }

    override fun createXSubscriber(): XSubscriberSocket {
        TODO("Not yet implemented")
    }

    override fun createPush(): PushSocket = wrapping {
        JeroMQPushSocket(newSocket(SocketType.PUSH))
    }

    override fun createPull(): PullSocket = wrapping {
        JeroMQPullSocket(newSocket(SocketType.PULL))
    }

    override fun createRequest(): RequestSocket {
        TODO("Not yet implemented")
    }

    override fun createReply(): ReplySocket {
        TODO("Not yet implemented")
    }

    override fun createDealer(): DealerSocket {
        TODO("Not yet implemented")
    }

    override fun createRouter(): RouterSocket {
        TODO("Not yet implemented")
    }

    private fun newSocket(type: SocketType): ZMQ.Socket = underlying.createSocket(type)
}
