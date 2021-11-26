/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

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
