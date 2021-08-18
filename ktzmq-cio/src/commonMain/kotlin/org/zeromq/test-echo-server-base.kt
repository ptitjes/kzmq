package org.zeromq

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@OptIn(InternalAPI::class)
suspend fun startEchoServer(dispatcher: CoroutineContext) = coroutineScope {
    val selectorManager = SelectorManager(dispatcher)
    val server = aSocket(selectorManager).tcp()
        .bind(NetworkAddress("127.0.0.1", 2323))

    println("Started echo telnet server at ${server.localAddress}")

    while (true) {
        val socket = server.accept()

        launch {
            println("Socket accepted: ${socket.remoteAddress}")

            val input = socket.openReadChannel()
            val output = socket.openWriteChannel(autoFlush = true)

            try {
                while (true) {
                    val line = input.readUTF8Line()

                    println("${socket.remoteAddress}: $line")
                    output.writeStringUtf8("$line\r\n")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                socket.close()
            }
        }
    }
}
