/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Pull as ZPull

internal class ZeroMQJsPullSocket internal constructor(
    override val underlying: ZPull = ZPull(),
) :
    ZeroMQJsSocket(Type.PUB),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    PullSocket {

    override var conflate: Boolean by underlying::conflate
}
