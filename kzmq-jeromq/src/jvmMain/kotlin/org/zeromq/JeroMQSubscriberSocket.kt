/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQSubscriberSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.SUB, Type.SUB), SubscriberSocket {

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by notImplementedProperty()
}
