/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JSInstance : EngineInstance {

    override fun close() {}

    override fun createPair(): PairSocket = ZeroMQJsPairSocket()
    override fun createPublisher(): PublisherSocket = ZeroMQJsPublisherSocket()
    override fun createSubscriber(): SubscriberSocket = ZeroMQJsSubscriberSocket()
    override fun createXPublisher(): XPublisherSocket = ZeroMQJsXPublisherSocket()
    override fun createXSubscriber(): XSubscriberSocket = ZeroMQJsXSubscriberSocket()
    override fun createPush(): PushSocket = ZeroMQJsPushSocket()
    override fun createPull(): PullSocket = ZeroMQJsPullSocket()
    override fun createRequest(): RequestSocket = ZeroMQJsRequestSocket()
    override fun createReply(): ReplySocket = ZeroMQJsReplySocket()
    override fun createDealer(): DealerSocket = ZeroMQJsDealerSocket()
    override fun createRouter(): RouterSocket = ZeroMQJsRouterSocket()
}
