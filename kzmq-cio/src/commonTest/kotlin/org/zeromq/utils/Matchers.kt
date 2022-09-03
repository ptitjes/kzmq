/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import io.kotest.assertions.*
import io.kotest.assertions.print.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*
import kotlin.jvm.*

@JvmName("messageChannelShouldReceiveExactly")
internal suspend infix fun ReceiveChannel<Message>.shouldReceiveExactly(expected: List<Message>) {
    shouldReceiveExactly(expected) { receive() }
}

@JvmName("commandOrMessageChannelShouldReceiveExactly")
internal suspend infix fun ReceiveChannel<CommandOrMessage>.shouldReceiveExactly(expected: List<Message>) {
    shouldReceiveExactly(expected) { receive().messageOrThrow() }
}

private suspend fun shouldReceiveExactly(expected: List<Message>, receive: suspend () -> Message) {
    val received = mutableListOf<Message>()
    try {
        repeat(expected.size) {
            received += receive()
        }
        received shouldContainExactly expected
    } catch (e: TimeoutCancellationException) {
        throw failure(
            Expected(expected.print()),
            Actual(received.print()),
            "Only ${received.size} of the expected ${expected.size} messages were received.",
        )
    }
}
