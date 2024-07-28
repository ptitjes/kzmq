/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Dealer as ZDealer

internal class ZeroMQJsDealerSocket internal constructor(
    override val underlying: ZDealer = ZDealer(),
) :
    ZeroMQJsSocket(Type.DEALER),
    SendSocket by ZeroMQJsSendSocket(underlying),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    DealerSocket {

    override var conflate: Boolean by underlying::conflate

    override var routingId: ByteArray? by underlying::routingId.asNullableByteArrayProperty()

    override var probeRouter: Boolean by underlying::probeRouter
}
