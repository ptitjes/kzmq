/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*

internal class JeroMQDealerSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.DEALER, Type.DEALER), DealerSocket {

    override var conflate: Boolean by underlying::conflate
    override var routingId: ByteString? by underlying::identity.converted()

    // TODO there no getter for setProbeRouter in underlying socket
    override var probeRouter: Boolean by notImplementedProperty()
}
