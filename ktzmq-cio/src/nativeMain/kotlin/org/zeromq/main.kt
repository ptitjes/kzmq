package org.zeromq

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    val dispatcher = newSingleThreadContext("IO")
    val context = Context(CIO, coroutineContext + dispatcher)

    context.publishEverySecond("Publisher") {
        connect("tcp://localhost:9990")
    }
}

private suspend fun Context.publishEverySecond(
    name: String,
    configure: suspend PublisherSocket.() -> Unit
) {
    with(createPublisher()) {
        configure()

        var value = 0
        while (true) {
            val data = "$name > $value"
            send(Message(data.encodeToByteArray()))
            println("Sent: $data")
            delay(1000)
            value++
        }
    }
}
