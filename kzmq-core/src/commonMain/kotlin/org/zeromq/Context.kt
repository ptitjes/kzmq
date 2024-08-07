/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

public expect val engines: List<EngineFactory>

public fun CoroutineScope.Context(
    additionalContext: CoroutineContext = EmptyCoroutineContext,
): Context = engines.firstOrNull()?.let { Context(it, additionalContext) } ?: error(
    "Failed to find ZeroMQ engine implementation in the classpath: consider adding engine dependency. " +
        "See https://github.com/ptitjes/kzmq#gradle"
)

/**
 * Builds a ZeroMQ [Context] in the receiver [CoroutineScope].
 *
 * @param engineFactory the backend engine to use for this context.
 * @param additionalContext additional context to [CoroutineScope.coroutineContext] context of the coroutine.
 */
public fun CoroutineScope.Context(
    engineFactory: EngineFactory,
    additionalContext: CoroutineContext = EmptyCoroutineContext,
): Context {
    val newContext = coroutineContext + additionalContext
    return Context(newContext, engineFactory)
}

/**
 * A ZeroMQ [Context].
 *
 * @param coroutineContext the parent coroutine context.
 * @param engineFactory the backend engine for this context.
 */
public class Context internal constructor(
    coroutineContext: CoroutineContext,
    engineFactory: EngineFactory,
) : SocketFactory, AutoCloseable {

    private val instance: Engine = engineFactory.create(coroutineContext)

    /**
     * Returns the set of transport schemes that this engine supports.
     */
    public val supportedTransports: Set<String> = engineFactory.supportedTransports

    /**
     * Closes this context.
     */
    override fun close(): Unit = instance.close()

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
