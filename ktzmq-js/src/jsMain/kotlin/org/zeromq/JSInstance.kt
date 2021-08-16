package org.zeromq

internal class JSInstance : EngineInstance {
    override fun createPublisher(): PublisherSocket = ZeroMQJsPublisherSocket()
    override fun createSubscriber(): SubscriberSocket = ZeroMQJsSubscriberSocket()
}
