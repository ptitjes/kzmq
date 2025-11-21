/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.test

import io.kotest.assertions.*
import io.kotest.assertions.print.*
import io.kotest.matchers.equals.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import kotlin.jvm.*

@JvmName("shouldReceiveExactlyListMessageTemplate")
public suspend fun shouldReceiveExactly(
    expected: List<MessageTemplate>,
    receive: suspend () -> Message,
) {
    shouldReceiveExactly(expected.map { it.frames }, receive)
}

@JvmName("shouldReceiveExactlyListListByteString")
private suspend fun shouldReceiveExactly(
    expected: List<List<ByteString>>,
    receive: suspend () -> Message,
) {
    val received = mutableListOf<List<ByteString>>()
    try {
        repeat(expected.size) {
            val message = receive()
            received += message.readFrames().map { it.readByteString() }
        }

        received shouldBeEqual expected
    } catch (e: TimeoutCancellationException) {
        throw AssertionErrorBuilder(
            message = "Only ${received.size} of the expected ${expected.size} messages were received.",
            cause = e,
            expected = Expected(expected.print()),
            actual = Actual(received.print())
        ).build()
    }
}
