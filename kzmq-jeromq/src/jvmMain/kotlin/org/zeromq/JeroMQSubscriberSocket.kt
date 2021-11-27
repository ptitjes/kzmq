/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQSubscriberSocket internal constructor(
    underlying: ZMQ.Socket,
) : JeroMQSocket(underlying, Type.SUB), SubscriberSocket {

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by notImplementedProperty()
}
