/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.internal.*
import org.zeromq.internal.inproc.*
import org.zeromq.internal.tcp.*
import kotlin.coroutines.*

internal class CIOEngine internal constructor(
    context: CoroutineContext,
) : Engine {

    private val mainJob = SupervisorJob()
    private val lingerJob = SupervisorJob()

    internal val mainScope = CoroutineScope(context + mainJob)
    internal val lingerScope = CoroutineScope(context + lingerJob)

    val transportRegistry = TransportRegistry(
        listOf(
            TcpTransport(mainScope.coroutineContext),
            InprocTransport(mainScope.coroutineContext),
        )
    )

    override fun close() {
        transportRegistry.close()
        mainJob.cancel()
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
