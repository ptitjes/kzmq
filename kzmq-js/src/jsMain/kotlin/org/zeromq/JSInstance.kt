/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JSInstance : EngineInstance {

    override fun close() {}

    override fun createPublisher(): PublisherSocket = ZeroMQJsPublisherSocket()
    override fun createSubscriber(): SubscriberSocket = ZeroMQJsSubscriberSocket()

    override fun createPair(): PairSocket {
        TODO("Not yet implemented")
    }

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