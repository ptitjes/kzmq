package org.zeromq

import org.zeromq.internal.SelectorManager

internal const val TRACE = false

internal class JeroMQInstance private constructor(
    private val underlying: ZContext
) : EngineInstance {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads))

    private val selector = SelectorManager()

    override fun createPublisher(): PublisherSocket = wrappingExceptions {
        JeroMQPublisherSocket(selector, newSocket(SocketType.PUB))
    }

    override fun createSubscriber(): SubscriberSocket = wrappingExceptions {
        JeroMQSubscriberSocket(selector, newSocket(SocketType.SUB))
    }

    private fun newSocket(type: SocketType): ZMQ.Socket = underlying.createSocket(type)
}
