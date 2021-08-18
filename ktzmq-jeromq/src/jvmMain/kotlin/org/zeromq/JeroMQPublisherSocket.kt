package org.zeromq

internal class JeroMQPublisherSocket internal constructor(underlying: ZMQ.Socket) :
    JeroMQSocket(underlying), PublisherSocket
