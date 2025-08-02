/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*

internal class JeroMQDealerSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.DEALER, Type.DEALER), DealerSocket {

    override var conflate: Boolean by underlying::conflate
    override var routingId: ByteString?
        get() = underlying.identity?.let { ByteString(it) }
        set(value) {
            underlying.identity = value?.toByteArray()
        }

    // TODO there no getter for setProbeRouter in underlying socket
    override var probeRouter: Boolean by notImplementedProperty()
}
