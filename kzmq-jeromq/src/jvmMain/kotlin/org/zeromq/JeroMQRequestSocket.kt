/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQRequestSocket internal constructor(
    underlying: ZMQ.Socket,
) : JeroMQSocket(underlying, Type.REQ), RequestSocket {

    override var routingId: String? by notImplementedProperty()
    override var probeRouter: Boolean by notImplementedProperty()
    override var correlate: Boolean by notImplementedProperty()
    override var relaxed: Boolean by notImplementedProperty()
}
