package org.zeromq

internal class JeroMQPublisherSocket internal constructor(
    context: JeroMQInstance,
    underlying: ZMQ.Socket
) : JeroMQSocket(context, underlying), PublisherSocket
