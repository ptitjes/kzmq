/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.transports

import io.kotest.core.spec.style.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.sockets.*
import org.zeromq.tests.utils.*
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
class InprocTests : FunSpec({

    withContext("bind-connect").config(
        skipEngines = listOf("jeromq"),
        timeout = 5.seconds,
    ) { ctx ->
        val address = randomAddress(Protocol.INPROC)

        val push = ctx.createPush()
        push.bind(address)

        val pull1 = ctx.createPull()
        pull1.connect(address)

        val pull2 = ctx.createPull()
        pull2.connect(address)

        waitForSubscriptions()

        testRoundRobinDispatch(push, pull1, pull2)
    }

    withContext("connect-bind").config(
        skipEngines = listOf("jeromq")
    ) { ctx ->
        val address1 = randomAddress(Protocol.INPROC)
        val address2 = randomAddress(Protocol.INPROC)

        val push = ctx.createPush()
        push.connect(address1)
        push.connect(address2)

        val pull1 = ctx.createPull()
        pull1.bind(address1)

        val pull2 = ctx.createPull()
        pull2.bind(address2)

        waitForSubscriptions()

        testRoundRobinDispatch(push, pull1, pull2)
    }
})

private suspend fun testRoundRobinDispatch(push: PushSocket, pull1: PullSocket, pull2: PullSocket) {
    val message1 = Message("Hello 1".encodeToByteArray())
    val message2 = Message("Hello 2".encodeToByteArray())

    var received1: Message? = null
    var received2: Message? = null

    try {
        coroutineScope {
            launch {
                push.send(message1)
                push.send(message2)
            }
            launch { received1 = pull1.receive() }
            launch { received2 = pull2.receive() }
        }
    } finally {
        setOf(received1, received2) shouldContainExactly setOf(message1, message2)
    }
}
