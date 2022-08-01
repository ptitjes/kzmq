/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package temp

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*

fun main(): Unit = runBlocking {
    val dispatcher = Dispatchers.IO

    val channel1 = Channel<Message>()
    val channel2 = Channel<Message>()

    val messageSize = 100
    val messageCount = 10_000_000
    val message = Message(ByteArray(messageSize))

    withContext(dispatcher) {
        val pushJob = launch {
            channel1.push(message, messageCount)
        }
        val forwardJob = launch {
            for (message in channel1) {
                channel2.send(message)
            }
        }
        val pullJob = launch {
            channel2.pull(messageCount)
        }
        pullJob.join()
        forwardJob.cancelAndJoin()
        pushJob.cancelAndJoin()
    }
}

private suspend fun Channel<Message>.push(
    message: Message,
    messageCount: Int,
) {
    var sent = 0

    while (sent < messageCount) {
        send(message)
        sent++
    }
}

private suspend fun Channel<Message>.pull(
    messageCount: Int,
) {
    var received = 0
    val start = System.currentTimeMillis()

    try {
        while (received < messageCount) {
            receive()
            received++
        }
    } finally {
        val time = (System.currentTimeMillis() - start).toDouble() / 1_000
        val throughput = received / time
        println("Received $received messages in $time seconds ($throughput messages/s)")
    }
}
