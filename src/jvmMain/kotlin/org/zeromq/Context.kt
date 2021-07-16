package org.zeromq

actual class Context private constructor(private val underlying: ZContext) {

    actual constructor() : this(ZContext()) {}
    actual constructor(ioThreads: Int) : this(ZContext(ioThreads)) {}

    actual fun createSocket(type: Type): Socket =
        SocketImpl(underlying.createSocket(SocketType.type(type.type)))

    actual companion object {
        actual fun shadow(context: Context) = Context(ZContext.shadow(context.underlying))
    }
}
