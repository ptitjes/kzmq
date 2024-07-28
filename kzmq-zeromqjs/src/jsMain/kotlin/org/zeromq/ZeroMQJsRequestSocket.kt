/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Request as ZRequest

internal class ZeroMQJsRequestSocket internal constructor(
    override val underlying: ZRequest = ZRequest(),
) :
    ZeroMQJsSocket(Type.PUB),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    SendSocket by ZeroMQJsSendSocket(underlying),
    RequestSocket {

    override var routingId: ByteArray? by underlying::routingId.asNullableByteArrayProperty()
    override var probeRouter: Boolean by underlying::probeRouter
    override var correlate: Boolean by underlying::correlate
    override var relaxed: Boolean by underlying::relaxed
}
