/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package temp

import kotlinx.coroutines.*
import org.zeromq.*

fun mainSubscriber(): Unit = runBlocking {
    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(CIO, coroutineContext + handler)

    context.pull {
//        bind("ipc:///tmp/zmq-test")
        connect("tcp://localhost:9999")
    }
}

private suspend fun Context.pull(
    configure: SubscriberSocket.() -> Unit
) {
    with(createSubscriber()) {
        configure()
        subscribe()

        for (message in this) {
            val data = message.singleOrThrow().decodeToString()
            println("Received: $data")
        }
    }
}
