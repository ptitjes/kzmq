/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.sockets.*
import kotlin.time.Duration.Companion.milliseconds

suspend fun testRoundRobin(
    sender: suspend (Message) -> Unit,
    receivers: List<suspend () -> Message>,
    messageCount: Int = 10,
) {
    val messages = List(messageCount) { index -> Message(index.packToByteArray()) }

    // Send each message once per receiver
    messages.forEach { message -> repeat(receivers.size) { sender(message) } }

    // Check each receiver got every messages
    receivers.forEach { receiver ->
        List(messages.size) { receiver() } shouldBe messages
    }
}

suspend fun testFairQueuing(
    senders: List<suspend (Message) -> Unit>,
    receiver: suspend () -> Message,
    messageCount: Int = 10,
) {
    // Send a consecutive messages for each sender
    senders.forEachIndexed { senderIndex, sender ->
        repeat(messageCount) { messageIndex ->
            sender(Message(messageIndex.packToByteArray(), senderIndex.packToByteArray()))
        }
    }

    // Wait for messages to fill the receiver's peer queues
    delay(500.milliseconds)

    // Check we received all messages fair-queued
    repeat(messageCount) { messageIndex ->
        List(senders.size) { receiver() }.toSet() shouldBe List(senders.size) { senderIndex ->
            Message(messageIndex.packToByteArray(), senderIndex.packToByteArray())
        }.toSet()
    }
}
