package org.zeromq

class Context(engine: Engine) : SocketFactory {
    private val instance: EngineInstance = engine.createInstance()

    override fun createPair(): PairSocket = instance.createPair()
    override fun createPublisher(): PublisherSocket = instance.createPublisher()
    override fun createSubscriber(): SubscriberSocket = instance.createSubscriber()
    override fun createXPublisher(): XPublisherSocket = instance.createXPublisher()
    override fun createXSubscriber(): XSubscriberSocket = instance.createXSubscriber()
    override fun createPush(): PushSocket = instance.createPush()
    override fun createPull(): PullSocket = instance.createPull()
    override fun createRequest(): RequestSocket = instance.createRequest()
    override fun createReply(): ReplySocket = instance.createReply()
    override fun createDealer(): DealerSocket = instance.createDealer()
    override fun createRouter(): RouterSocket = instance.createRouter()
}
