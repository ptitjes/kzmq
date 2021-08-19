package org.zeromq

internal class JeroMQSubscriberSocket internal constructor(
    context: JeroMQInstance,
    underlying: ZMQ.Socket
) : JeroMQSocket(context, underlying), SubscriberSocket
