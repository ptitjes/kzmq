package org.zeromq

internal fun <T> wrappingExceptions(block: () -> T): T = try {
    block()
} catch (e: ZMQException) {
    throw ZeroMQException(e.errorCode, e)
}

internal fun <T> catchingExceptions(block: () -> T): SocketResult<T> = try {
    SocketResult.success(block())
} catch (e: ZMQException) {
    SocketResult.failure(e)
}

internal suspend fun <T> wrappingExceptionsSuspend(block: suspend () -> T): T = try {
    block()
} catch (e: ZMQException) {
    throw ZeroMQException(e.errorCode, e)
}

internal suspend fun <T> catchingExceptionsSuspend(block: suspend () -> T): SocketResult<T> = try {
    SocketResult.success(block())
} catch (e: ZMQException) {
    SocketResult.failure(e)
}
