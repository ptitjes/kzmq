/*
 * Copyright (c) 2023-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.benchmarks

import kotlinx.benchmark.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.random.*

@State(Scope.Benchmark)
open class PullPushBenchmark() {
    @Param("jeromq", "cio")
    var engineName = ""

    @Param("inproc", "ipc", "tcp")
    var transport = "tcp"

    private lateinit var address: String

    @Param("10", "100", "1000", "10000", "100000")
    var messageSize = 10

    private lateinit var message: Message

    private lateinit var scope: CoroutineScope
    private lateinit var context: Context

    private lateinit var pushSocket: PushSocket
    private lateinit var pullSocket: PullSocket

    @OptIn(ExperimentalStdlibApi::class)
    @Setup
    fun setUp() = runBlocking {
        address = when (transport) {
            "tcp" -> "tcp://127.0.0.1:9990"
            "ipc" -> "ipc:///tmp/test-${Random.nextBytes(16).toHexString(HexFormat.Default)}"
            "inproc" -> "inproc://test-${Random.nextBytes(16).toHexString(HexFormat.Default)}"
            else -> error("Unsuported transport '$transport'")
        }

        message = Message(ByteArray(messageSize))

        val engine = engines.find { it.name.lowercase() == engineName } ?: error("Engine '$engineName' not found")
        if (!engine.supportedTransports.contains(transport))
            error("Engine '$engineName' does not support transport '$transport'")

        scope = CoroutineScope(Dispatchers.IO)
        context = scope.Context(engine)

        pushSocket = context.createPush().apply { connect(address) }
        pullSocket = context.createPull().apply { bind(address) }
    }

    @TearDown
    fun tearDown() = runBlocking {
        pushSocket.close()
        pullSocket.close()
        context.close()
        scope.cancel()
    }

    @Benchmark
    fun sendReceive() = runBlocking {
        pushSocket.send(message)
        pullSocket.receive()
    }
}
