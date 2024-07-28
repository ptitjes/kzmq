/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Push as ZPush

internal class ZeroMQJsPushSocket internal constructor(
    override val underlying: ZPush = ZPush(),
) :
    ZeroMQJsSocket(Type.PUB),
    SendSocket by ZeroMQJsSendSocket(underlying),
    PushSocket {

    override var conflate: Boolean by underlying::conflate
}
