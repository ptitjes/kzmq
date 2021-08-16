package org.zeromq

interface EngineInstance {
    fun createPublisher(): PublisherSocket
    fun createSubscriber(): SubscriberSocket
}
