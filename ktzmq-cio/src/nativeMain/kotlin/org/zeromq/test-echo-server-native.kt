package org.zeromq

import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

fun main() = runBlocking {
    startEchoServer(coroutineContext)
}
