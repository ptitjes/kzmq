package org.zeromq

import org.zeromq.internal.SelectorManager

internal class JeroMQPublisherSocket internal constructor(
    selector: SelectorManager,
    underlying: ZMQ.Socket
) : JeroMQSocket(selector, underlying), PublisherSocket
