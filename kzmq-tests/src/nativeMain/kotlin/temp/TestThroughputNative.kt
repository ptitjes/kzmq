/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package temp

import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.system.*

fun mainThroughput(): Unit = runBlocking {
    val dispatcher = newFixedThreadPoolContext(4, "App")
    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(CIO, handler)

    val messageSize = 100
    val messageCount = 10_000_000
    val message = Message(ByteArray(messageSize))

    withContext(dispatcher) {
        val pushJob = launch(context) {
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
    configure: suspend PushSocket.() -> Unit,
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
    configure: suspend PullSocket.() -> Unit,
) {
    var received = 0

    val time = measureTimeMillis {
        with(createPull()) {
            configure()

            while (received < messageCount) {
                val message = receive()
//                releaseMessage(message)
                received++
            }
        }
    }
    val seconds = time.toDouble() / 1_000
    val throughput = received / seconds
    println("Received $received messages in $seconds seconds ($throughput messages/s)")
}
