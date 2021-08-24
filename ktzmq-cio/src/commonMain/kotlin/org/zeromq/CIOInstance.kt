package org.zeromq

import io.ktor.network.selector.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal const val TRACE = false

internal class CIOInstance internal constructor(
    override val coroutineContext: CoroutineContext,
) : EngineInstance, CoroutineScope {

    @OptIn(InternalAPI::class)
    private val selectorManager = SelectorManager(coroutineContext)

    override fun createPair(): PairSocket =
        CIOPairSocket(coroutineContext, selectorManager)

    override fun createPublisher(): PublisherSocket {
        TODO("Not yet implemented")
    }

    override fun createSubscriber(): SubscriberSocket =
        CIOSubscriberSocket(coroutineContext, selectorManager)

    override fun createXPublisher(): XPublisherSocket {
        TODO("Not yet implemented")
    }

    override fun createXSubscriber(): XSubscriberSocket {
        TODO("Not yet implemented")
    }

    override fun createPush(): PushSocket {
        TODO("Not yet implemented")
    }

    override fun createPull(): PullSocket {
        TODO("Not yet implemented")
    }

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
