/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*

internal class JeroMQRequestSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.REQ, Type.REQ), RequestSocket {

    override var routingId: ByteString? by underlying::identity.converted()
    override var probeRouter: Boolean by notImplementedProperty()
    override var correlate: Boolean by notImplementedProperty()
    override var relaxed: Boolean by notImplementedProperty()
}
