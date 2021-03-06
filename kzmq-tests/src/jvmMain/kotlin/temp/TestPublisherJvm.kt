/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package temp

import kotlinx.coroutines.*
import org.zeromq.*

fun main(): Unit = runBlocking {
    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(JeroMQ, coroutineContext + handler)

    context.publishEverySecond("Publisher") {
//        connect("ipc:///tmp/zmq-test")
//        connect("tcp://localhost:9999")
        bind("tcp://localhost:9999")
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
