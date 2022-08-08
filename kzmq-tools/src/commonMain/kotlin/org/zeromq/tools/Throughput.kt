/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tools

import kotlinx.cli.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.time.*

private const val DEFAULT_ADDRESS = "tcp://127.0.0.1:9990"
private const val DEFAULT_MESSAGE_COUNT = 10_000_000
private const val DEFAULT_MESSAGE_SIZE = 100

fun main(args: Array<String>) = runBlocking {
    val parser = ArgParser("throughput")

    val engineName by parser.option(ArgType.String, fullName = "engine-name", description = "Engine name")
        .required()

    val address by parser.option(ArgType.String, fullName = "address", description = "Address")
        .default(DEFAULT_ADDRESS)

    val messageCount by parser.option(ArgType.Int, fullName = "message-count", description = "Message count")
        .default(DEFAULT_MESSAGE_COUNT)
    val messageSize by parser.option(ArgType.Int, fullName = "message-size", description = "Message size")
        .default(DEFAULT_MESSAGE_SIZE)

    val side by parser.option(
        ArgType.Choice<Side>(),
        fullName = "side",
        description = "Throughput test side ('push', 'pull', or 'both')"
    ).default(Side.BOTH)

    parser.parse(args)

    val engine = engines.find { it.name == engineName } ?: error("No such engine: $engineName")

    val message = Message(ByteArray(messageSize))

    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(engine, coroutineContext + handler + dispatcher)

    withContext(dispatcher) {
        when (side) {
            Side.PUSH -> {
                val pushJob = launch { context.push(message, messageCount) { connect(address) } }
                pushJob.join()
            }

            Side.PULL -> {
                val pullJob = launch { context.pull(messageCount) { bind(address) } }
                pullJob.join()
            }

            Side.BOTH -> {
                val pushJob = launch { context.push(message, messageCount) { connect(address) } }
                val pullJob = launch { context.pull(messageCount) { bind(address) } }

                pullJob.join()
                pushJob.cancelAndJoin()
            }
        }

        context.close()
    }
}

private enum class Side {
    PUSH, PULL, BOTH
}

private suspend fun Context.push(
    message: Message,
    messageCount: Int,
    configure: suspend PushSocket.() -> Unit,
) {
    createPush().apply { configure() }.use { socket ->
        var sent = 0
        while (sent < messageCount) {
            socket.send(message)
            sent++
            if (sent % 1000 == 0) println("Sent $sent")
        }
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun Context.pull(
    messageCount: Int,
    configure: suspend PullSocket.() -> Unit,
) {
    createPull().apply { configure() }.use { socket ->
        var received = 0
        val elapsed = measureTime {
            while (received < messageCount) {
                socket.receive()
                received++
                if (received % 1000 == 0) println("Received $received")
            }
        }.toDouble(DurationUnit.SECONDS)

        val throughput = received / elapsed
        println("Received $received messages in $elapsed seconds ($throughput messages/s)")
    }
}
