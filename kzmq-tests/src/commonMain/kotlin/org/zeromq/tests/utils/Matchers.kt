/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.kotest.assertions.*
import io.kotest.assertions.print.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import org.zeromq.*

suspend infix fun ReceiveSocket.shouldReceiveExactly(expected: List<Message>) {
    shouldReceiveExactly(expected) { receive() }
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
