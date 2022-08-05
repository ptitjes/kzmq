/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.transports

import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.time.Duration.Companion.milliseconds

internal suspend fun simpleBindConnect(ctx1: Context, ctx2: Context, address: String) {
    val message = Message("Hello 0MQ!".encodeToByteArray())

    val push = ctx1.createPush().apply { bind(address) }
    val pull = ctx2.createPull().apply { connect(address) }

    // Wait for connection
    delay(100.milliseconds)

    push.send(message)
    pull.receive() shouldBe message
}

internal suspend fun simpleConnectBind(ctx1: Context, ctx2: Context, address: String) {
    val message = Message("Hello 0MQ!".encodeToByteArray())

    val push = ctx1.createPush().apply { connect(address) }
    val pull = ctx2.createPull().apply { bind(address) }

    // Wait for connection
    delay(100.milliseconds)

    push.send(message)
    pull.receive() shouldBe message
}
