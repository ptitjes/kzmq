/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*

internal class JeroMQReplySocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.REP, Type.REP), ReplySocket {

    override var routingId: ByteString?
        get() = underlying.identity?.let { ByteString(it) }
        set(value) {
            underlying.identity = value?.toByteArray()
        }
}
