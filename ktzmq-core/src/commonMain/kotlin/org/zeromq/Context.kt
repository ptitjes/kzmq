package org.zeromq

class Context(engine: Engine) {
    private val instance: EngineInstance = engine.createInstance()

    fun createPublisher(): PublisherSocket = instance.createPublisher()
    fun createSubscriber(): SubscriberSocket = instance.createSubscriber()
}
