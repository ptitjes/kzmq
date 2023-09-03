/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tools

import kotlinx.cli.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.time.*

fun main(args: Array<String>) = runBlocking {
    val parser = ArgParser("throughput")

    val engineName by parser.option(ArgType.String, fullName = "engine-name", description = "Engine name")
        .required()

    val address by parser.option(ArgType.String, fullName = "address", description = "Address")
        .default("tcp://127.0.0.1:9990")

    val messageCount by parser.option(ArgType.Int, fullName = "message-count", description = "Message count")
        .default(10_000_000)
    val messageSize by parser.option(ArgType.Int, fullName = "message-size", description = "Message size")
        .default(100)

    val verbose by parser.option(ArgType.Boolean, fullName = "verbose", description = "Set verbose output")
        .default(false)

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
                val pushJob = launch { context.push(message, messageCount, verbose) { connect(address) } }
                pushJob.join()
            }

            Side.PULL -> {
                val pullJob = launch { context.pull(messageCount, verbose) { bind(address) } }
                pullJob.join()
            }

            Side.BOTH -> {
                val pushJob = launch { context.push(message, messageCount, verbose) { connect(address) } }
                val pullJob = launch { context.pull(messageCount, verbose) { bind(address) } }

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
    verbose: Boolean,
    configure: suspend PushSocket.() -> Unit,
) {
    createPush().apply { configure() }.use { socket ->
        var sent = 0
        while (sent < messageCount) {
            socket.send(message)
            sent++
            if (verbose && sent % 1000 == 0) println("Sent $sent")
        }
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun Context.pull(
    messageCount: Int,
    verbose: Boolean,
    configure: suspend PullSocket.() -> Unit,
) {
    createPull().apply { configure() }.use { socket ->
        var received = 0
        val elapsed = measureTime {
            while (received < messageCount) {
                socket.receive()
                received++
                if (verbose && received % 1000 == 0) println("Received $received")
            }
        }.toDouble(DurationUnit.SECONDS)

        val throughput = received / elapsed
        println("Received $received messages in $elapsed seconds ($throughput messages/s)")
    }
}
