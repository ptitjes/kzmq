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

    context.pull {
//        bind("ipc:///tmp/zmq-test")
//        bind("tcp://localhost:9999")
        connect("tcp://localhost:9999")
    }
}

private suspend fun Context.pull(
    configure: SubscriberSocket.() -> Unit
) {
    with(createSubscriber()) {
        configure()
        subscribe("")

        for (message in this) {
            val data = message.removeFirst().copyOf().decodeToString()
            println("Received: $data")
        }
    }
}
