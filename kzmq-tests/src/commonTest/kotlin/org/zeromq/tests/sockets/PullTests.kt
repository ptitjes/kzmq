/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.assertions.*
import io.kotest.common.*
import io.kotest.core.spec.style.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@OptIn(ExperimentalKotest::class)
@Suppress("unused")
class PullTests : FunSpec({

    withContexts("SHALL receive incoming messages from its peers using a fair-queuing strategy").config(
        // TODO investigate why these tests are flaky
        only = setOf(),
    ) { ctx1, ctx2, protocol ->
        // TODO Investigate why this fails with CIO native
        if (platform == Platform.Native) return@config

        val address = randomAddress(protocol)

        val pushSocketCount = 5
        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSockets = List(pushSocketCount) { ctx1.createPush().apply { connect(address) } }

        waitForConnections(pushSocketCount)

        val messages = List(10) { index -> Message(ByteArray(1) { index.toByte() }) }

        pushSockets.forEach { pushSocket ->
            messages.forEach { message ->
                pushSocket.send(message)
            }
        }

        all {
            messages.forEach { message ->
                pullSocket shouldReceiveExactly List(pushSocketCount) { message }
            }
        }
    }
})
