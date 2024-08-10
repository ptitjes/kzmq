/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tools

import kotlinx.cli.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

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

    val messageContent = buildByteString(messageSize) {}

    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(engine, coroutineContext + handler + dispatcher)

    withContext(dispatcher) {
        when (side) {
            Side.PUSH -> {
                val pushJob = launch {
                    context.push(messageContent, messageCount, verbose) {
                        println("Connecting to $address")
                        connect(address)
                    }
                }
                pushJob.join()
            }

            Side.PULL -> {
                val pullJob = launch {
                    context.pull(messageContent, messageCount, verbose) {
                        println("Binding to $address")
                        bind(address)
                    }
                }
                pullJob.join()
            }

            Side.BOTH -> {
                val pushJob = launch {
                    context.push(messageContent, messageCount, verbose) {
                        println("Connecting to $address")
                        connect(address)
                    }
                }
                val pullJob = launch {
                    context.pull(messageContent, messageCount, verbose) {
                        println("Binding to $address")
                        bind(address)
                    }
                }

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
    messageContent: ByteString,
    messageCount: Int,
    verbose: Boolean,
    configure: suspend PushSocket.() -> Unit,
) = withContext(Dispatchers.IO) {
    println("Creating Push socket")
    createPush().apply { configure() }.use { socket ->
        delay(1.seconds)

        var sent = 0
        while (sent < messageCount) {
            socket.send { writeFrame(messageContent) }
            sent++
            if (verbose && sent % 1000 == 0) println("Sent $sent")
        }
    }
}

private suspend fun Context.pull(
    messageContent: ByteString,
    messageCount: Int,
    verbose: Boolean,
    configure: suspend PullSocket.() -> Unit,
) = withContext(Dispatchers.IO) {
    val messageSize = messageContent.size
    println("Creating Pull socket")
    createPull().apply { configure() }.use { socket ->
        delay(1.seconds)

        var received = 0
        val elapsed = measureTime {
            while (received < messageCount) {
                socket.receive { readFrame { skip(messageSize.toLong()) } }
                received++
                if (verbose && received % 1000 == 0) println("Received $received")
            }
        }.toDouble(DurationUnit.SECONDS)

        val throughput = received / elapsed
        println("Received $received messages in $elapsed seconds ($throughput messages/s)")
    }
}
