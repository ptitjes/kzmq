/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQXPublisherSocket internal constructor(
    factory: (type: SocketType) -> ZMQ.Socket,
) : JeroMQSocket(factory, SocketType.XPUB, Type.XPUB), XPublisherSocket {

    override var invertMatching: Boolean by notImplementedProperty()

    // TODO there no getter for setXpubNoDrop in underlying socket
    override var noDrop: Boolean by notImplementedProperty()

    //    override var verbose: Boolean by underlying::verbose
    //    override var verboser: Boolean by underlying::verboser

    // TODO there no getter for setXpubManual in underlying socket
    override var manual: Boolean by notImplementedProperty()

    // TODO is there no option for this in JeroMQ?
    override var welcomeMessage: String? by notImplementedProperty()
}
