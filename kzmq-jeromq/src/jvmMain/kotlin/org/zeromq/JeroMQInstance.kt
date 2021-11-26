/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.internal.*

internal const val TRACE = false

internal class JeroMQInstance private constructor(
    private val underlying: ZContext,
) : EngineInstance {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads))

    private val selector = ActorSelectorManager(Dispatchers.IO)

    override fun close() {
        selector.close()
        underlying.close()
    }

    override fun createPublisher(): PublisherSocket = wrappingExceptions {
        JeroMQPublisherSocket(selector, newSocket(SocketType.PUB))
    }

    override fun createSubscriber(): SubscriberSocket = wrappingExceptions {
        JeroMQSubscriberSocket(selector, newSocket(SocketType.SUB))
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

    override fun createPush(): PushSocket = wrappingExceptions {
        JeroMQPushSocket(selector, newSocket(SocketType.PUSH))
    }

    override fun createPull(): PullSocket = wrappingExceptions {
        JeroMQPullSocket(selector, newSocket(SocketType.PULL))
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
