/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.*

internal class JeroMQPushSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying, Type.PUSH), PushSocket {

    override var conflate: Boolean by underlying::conflate
}
