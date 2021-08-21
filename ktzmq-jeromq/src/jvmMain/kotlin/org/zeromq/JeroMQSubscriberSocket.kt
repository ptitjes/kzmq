package org.zeromq

import org.zeromq.internal.SelectorManager

internal class JeroMQSubscriberSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying), SubscriberSocket {

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by notImplementedProperty()
}
