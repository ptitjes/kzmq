/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQPushSocket internal constructor(
    underlying: ZMQ.Socket,
) : JeroMQSocket(underlying, Type.PUSH), PushSocket {

    override var conflate: Boolean by underlying::conflate
}