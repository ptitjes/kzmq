/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*

internal class JeroMQRequestSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.REQ, Type.REQ), RequestSocket {

    override var routingId: ByteString?
        get() = underlying.identity?.let { ByteString(it) }
        set(value) {
            underlying.identity = value?.toByteArray()
        }
    override var probeRouter: Boolean by notImplementedProperty()
    override var correlate: Boolean by notImplementedProperty()
    override var relaxed: Boolean by notImplementedProperty()
}
