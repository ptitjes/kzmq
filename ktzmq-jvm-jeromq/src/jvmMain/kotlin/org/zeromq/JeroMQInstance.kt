package org.zeromq

internal class JeroMQInstance private constructor(private val underlying: ZContext) :
    EngineInstance {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads))

    override fun createPublisher(): PublisherSocket = wrapException {
        JeroMQPublisherSocket(underlying.createSocket(SocketType.PUB))
    }

    override fun createSubscriber(): SubscriberSocket = wrapException {
        JeroMQSubscriberSocket(underlying.createSocket(SocketType.SUB))
    }
}
