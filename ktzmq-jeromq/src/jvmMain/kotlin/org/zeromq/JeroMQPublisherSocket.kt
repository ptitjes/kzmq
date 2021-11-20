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
