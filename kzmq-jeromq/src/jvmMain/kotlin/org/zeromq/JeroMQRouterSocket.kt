/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQRouterSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.ROUTER, Type.ROUTER), RouterSocket {

    override var routingId: ByteArray? by underlying::identity

    // TODO there no getter for setProbeRouter in underlying socket
    override var probeRouter: Boolean by notImplementedProperty()

    // TODO there no getter for setRouterMandatory in underlying socket
    override var mandatory: Boolean by notImplementedProperty()

    // TODO there no getter for setRouterHandover in underlying socket
    override var handover: Boolean by notImplementedProperty()
}
