package org.zeromq

import kotlinx.coroutines.Dispatchers
import org.zeromq.internal.ActorSelectorManager

internal const val TRACE = false

internal class JeroMQInstance private constructor(
    private val underlying: ZContext
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

    override fun createPush(): PushSocket {
        TODO("Not yet implemented")
    }

    override fun createPull(): PullSocket {
        TODO("Not yet implemented")
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
