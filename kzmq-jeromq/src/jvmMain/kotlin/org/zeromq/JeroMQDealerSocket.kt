/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQDealerSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.DEALER, Type.DEALER), DealerSocket {

    override var conflate: Boolean by underlying::conflate

    // TODO is there no option for this in JeroMQ?
    override var routingId: String? by notImplementedProperty()

    // TODO there no getter for setProbeRouter in underlying socket
    override var probeRouter: Boolean by notImplementedProperty()
}
