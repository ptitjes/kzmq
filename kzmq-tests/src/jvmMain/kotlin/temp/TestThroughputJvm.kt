/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package temp

import kotlinx.coroutines.*
import org.zeromq.*

fun main(): Unit = runBlocking {
    val dispatcher = Dispatchers.IO
    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(CIO, coroutineContext + handler + dispatcher)

    val messageSize = 100
    val messageCount = 10_000_000
    val message = Message(ByteArray(messageSize))

    withContext(dispatcher) {
        val pushJob = launch {
            context.push(message, messageCount) { connect("tcp://localhost:9990") }
        }
        val pullJob = launch {
            context.pull(messageCount) { bind("tcp://localhost:9990") }
        }
        pullJob.join()
        pushJob.cancelAndJoin()
    }
}

private suspend fun Context.push(
    message: Message,
    messageCount: Int,
    configure: PushSocket.() -> Unit
) {
    var sent = 0
    with(createPush()) {
        configure()

        while (sent < messageCount) {
            send(message)
            sent++
        }
    }
}

private suspend fun Context.pull(
    messageCount: Int,
    configure: PullSocket.() -> Unit
) {
    var received = 0
    val start = System.currentTimeMillis()

    try {
        with(createPull()) {
            configure()

            while (received < messageCount) {
                val message = receive()
//                releaseMessage(message)
                received++
            }
        }
    } finally {
        val time = (System.currentTimeMillis() - start).toDouble() / 1_000
        val throughput = received / time
        println("Received $received messages in $time seconds ($throughput messages/s)")
//        displayStats()
    }
}
