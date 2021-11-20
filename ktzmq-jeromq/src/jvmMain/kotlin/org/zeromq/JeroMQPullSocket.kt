package org.zeromq

import org.zeromq.internal.*

internal class JeroMQPullSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying, Type.PULL), PullSocket {

    override var conflate: Boolean by underlying::conflate
}
