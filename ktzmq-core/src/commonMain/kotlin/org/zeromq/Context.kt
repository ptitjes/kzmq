package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

fun CoroutineScope.Context(
    engine: Engine,
    additionalContext: CoroutineContext = EmptyCoroutineContext
): Context {
    val newContext = coroutineContext + additionalContext
    return Context(newContext, engine)
}

class Context internal constructor(
    private val coroutineContext: CoroutineContext,
    private val engine: Engine
) : AbstractCoroutineContextElement(Context), SocketFactory {

    private val instance: EngineInstance = engine.createInstance(coroutineContext)

    fun close() = instance.close()

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

    /**
     * Key for [Context] instance in the coroutine context.
     */
    public companion object Key : CoroutineContext.Key<Context>
}
