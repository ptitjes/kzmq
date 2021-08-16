package org.zeromq

internal class JeroMQSubscriberSocket internal constructor(underlying: ZMQ.Socket) :
    JeroMQSocket(underlying), SubscriberSocket {
}
