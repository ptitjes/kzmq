/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.ktor.network.selector.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal class CIOInstance internal constructor(
    private val coroutineContext: CoroutineContext,
) : EngineInstance {

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    private val childContext = coroutineContext + job + handler

    @OptIn(InternalAPI::class)
    private val selectorManager = SelectorManager(childContext)

    override fun close() {
        selectorManager.close()
        job.cancel()
    }

    override fun createPair(): PairSocket {
        TODO("Not yet implemented")
    }

    override fun createPublisher(): PublisherSocket =
        CIOPublisherSocket(childContext, selectorManager)

    override fun createSubscriber(): SubscriberSocket =
        CIOSubscriberSocket(childContext, selectorManager)

    override fun createXPublisher(): XPublisherSocket {
        TODO("Not yet implemented")
    }

    override fun createXSubscriber(): XSubscriberSocket {
        TODO("Not yet implemented")
    }

    override fun createPush(): PushSocket =
        CIOPushSocket(childContext, selectorManager)

    override fun createPull(): PullSocket =
        CIOPullSocket(childContext, selectorManager)

    override fun createRequest(): RequestSocket {
        TODO("Not yet implemented")
    }

    override fun createReply(): ReplySocket {
        TODO("Not yet implemented")
    }

    override fun createDealer(): DealerSocket {
        TODO("Not yet implemented")
    }

    override fun createRouter(): RouterSocket {
        TODO("Not yet implemented")
    }
}
