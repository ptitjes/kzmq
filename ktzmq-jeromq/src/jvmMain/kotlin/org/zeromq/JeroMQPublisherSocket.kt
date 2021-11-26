/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.*

internal class JeroMQPublisherSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying, Type.PUB), PublisherSocket {

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by notImplementedProperty()

    // TODO there no getter for setXpubNoDrop in underlying socket
    override var noDrop: Boolean by notImplementedProperty()
}
