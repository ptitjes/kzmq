/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*

internal suspend fun <T> suspendOnIO(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block)

internal inline fun <T> wrapping(block: () -> T): T = try {
    block()
} catch (e: ZMQException) {
    throw ZeroMQException(e.errorCode, e)
}

internal inline fun <T> catching(block: () -> T): SocketResult<T> = try {
    SocketResult.success(block())
} catch (e: ZMQException) {
    SocketResult.failure(e)
}

internal inline fun <T> trace(function: String, block: () -> T): T = try {
    trace("$function - before")
    block()
} finally {
    trace("$function - after")
}

internal fun trace(message: String) {
    if (TRACE) println(message)
}
