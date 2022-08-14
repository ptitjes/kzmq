/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.transports

import io.kotest.matchers.*
import org.zeromq.*
import org.zeromq.tests.utils.*

internal suspend fun simpleBindConnect(ctx1: Context, ctx2: Context, address: String) {
    val message = Message("Hello 0MQ!".encodeToByteArray())

    val push = ctx1.createPush().apply { bind(address) }
    val pull = ctx2.createPull().apply { connect(address) }

    waitForConnections()

    push.send(message)
    pull.receive() shouldBe message
}

internal suspend fun simpleConnectBind(ctx1: Context, ctx2: Context, address: String) {
    val message = Message("Hello 0MQ!".encodeToByteArray())

    val pull = ctx2.createPull().apply { bind(address) }
    val push = ctx1.createPush().apply { connect(address) }

    waitForConnections()

    push.send(message)
    pull.receive() shouldBe message
}
