/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.internal.*
import org.zeromq.internal.tcp.*
import kotlin.coroutines.*

internal class CIOEngineInstance internal constructor(
    context: CoroutineContext,
) : EngineInstance, CoroutineScope {

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "An error occurred in CIO engine" }
    }
    override val coroutineContext = context + job + handler

    val transportRegistry = TransportRegistry(
        listOf(
            TcpTransport(coroutineContext),
        )
    )

    override fun close() {
        transportRegistry.close()
        job.cancel()
    }

    override fun createPair(): PairSocket = CIOPairSocket(this)
    override fun createPublisher(): PublisherSocket = CIOPublisherSocket(this)
    override fun createSubscriber(): SubscriberSocket = CIOSubscriberSocket(this)
    override fun createXPublisher(): XPublisherSocket = CIOXPublisherSocket(this)
    override fun createXSubscriber(): XSubscriberSocket = CIOXSubscriberSocket(this)
    override fun createPush(): PushSocket = CIOPushSocket(this)
    override fun createPull(): PullSocket = CIOPullSocket(this)
    override fun createRequest(): RequestSocket = CIORequestSocket(this)
    override fun createReply(): ReplySocket = CIOReplySocket(this)
    override fun createDealer(): DealerSocket = CIODealerSocket(this)
    override fun createRouter(): RouterSocket = CIORouterSocket(this)
}
