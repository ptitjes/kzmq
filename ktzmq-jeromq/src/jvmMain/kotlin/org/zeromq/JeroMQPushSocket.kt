package org.zeromq

import org.zeromq.internal.*

internal class JeroMQPushSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying, Type.PUSH), PushSocket {

    override var conflate: Boolean by underlying::conflate
}
