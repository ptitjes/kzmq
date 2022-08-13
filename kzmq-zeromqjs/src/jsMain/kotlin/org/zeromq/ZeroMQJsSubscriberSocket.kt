/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Subscriber as ZSubscriber

internal class ZeroMQJsSubscriberSocket internal constructor(override val underlying: ZSubscriber = ZSubscriber()) :
    ZeroMQJsSocket(Type.SUB),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    SubscriberSocket {

    override suspend fun subscribe() {
        underlying.subscribe()
    }

    override suspend fun subscribe(vararg topics: ByteArray) {
        underlying.subscribe(*topics.map { it.decodeToString() }.toTypedArray())
    }

    override suspend fun subscribe(vararg topics: String) {
        underlying.subscribe(*topics)
    }

    override suspend fun unsubscribe() {
        underlying.unsubscribe()
    }

    override suspend fun unsubscribe(vararg topics: ByteArray) {
        underlying.unsubscribe(*topics.map { it.decodeToString() }.toTypedArray())
    }

    override suspend fun unsubscribe(vararg topics: String) {
        underlying.unsubscribe(*topics)
    }

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by underlying::invertMatching
}
