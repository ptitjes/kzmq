package org.zeromq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    startEchoServer(Dispatchers.IO)
}
