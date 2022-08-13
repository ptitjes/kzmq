/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Router as ZRouter

internal class ZeroMQJsRouterSocket internal constructor(
    override val underlying: ZRouter = ZRouter(),
) :
    ZeroMQJsSocket(Type.ROUTER),
    SendSocket by ZeroMQJsSendSocket(underlying),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    RouterSocket {

    override var routingId: ByteArray? by underlying::routingId.asNullableByteArrayProperty()
    override var probeRouter: Boolean by underlying::probeRouter
    override var mandatory: Boolean by underlying::mandatory
    override var handover: Boolean by underlying::handover
}
