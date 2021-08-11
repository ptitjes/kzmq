package org.zeromq

class JeroMQContext private constructor(private val underlying: ZContext) : Context {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads)) {}

    override fun createSocket(type: Type): Socket = wrapException {
        JeroMQSocket(underlying.createSocket(SocketType.type(type.type)))
    }
}
