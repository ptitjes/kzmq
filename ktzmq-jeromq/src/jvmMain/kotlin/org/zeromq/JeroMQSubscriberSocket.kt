package org.zeromq

import org.zeromq.internal.SelectorManager

internal class JeroMQSubscriberSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying), SubscriberSocket
