/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQRequestSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.REQ, Type.REQ), RequestSocket {

    override var routingId: ByteArray? by underlying::identity
    override var probeRouter: Boolean by notImplementedProperty()
    override var correlate: Boolean by notImplementedProperty()
    override var relaxed: Boolean by notImplementedProperty()
}
