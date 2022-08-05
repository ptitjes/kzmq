/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.kotest.matchers.*
import org.zeromq.*

suspend fun testRoundRobinDispatch(
    sender: suspend (Message) -> Unit,
    receivers: List<suspend () -> Message>,
    messageCount: Int = 10,
) {
    val messages = List(messageCount) { index -> Message(ByteArray(1) { index.toByte() }) }

    // Send each message once per receiver
    messages.forEach { message -> repeat(receivers.size) { sender(message) } }

    // Check each receiver got every messages
    receivers.forEach { receiver ->
        List(messages.size) { receiver() } shouldBe messages
    }
}
