package org.zeromq

fun <T> wrappingExceptions(block: () -> T): T = try {
    block()
} catch (e: ZMQException) {
    throw ZeroMQException(e.errorCode, e)
}

fun <T> catchingExceptions(block: () -> T): SocketResult<T> = try {
    SocketResult.success(block())
} catch (e: ZMQException) {
    SocketResult.failure(e)
}
