/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.test

import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

private val SUSPENSION_HINT_TIMEOUT = 1.seconds

public suspend fun assertSuspends(block: suspend () -> Unit) {
    val result = withTimeoutOrNull(SUSPENSION_HINT_TIMEOUT) {
        block()
    }
    assertNull(result, "Expected block to suspend")
}

public suspend fun <T> assertDoesNotSuspend(block: suspend () -> T) {
    val result = withTimeoutOrNull(SUSPENSION_HINT_TIMEOUT) {
        block()
    }
    assertNotNull(result, "Expected block to not suspend")
}

public suspend fun assertReceivesExactly(expected: List<Message>, receive: suspend () -> Message) {
    val received = buildList {
        repeat(expected.size) {
            add(receive())
        }
    }

    assertEquals(expected, received/*, "Expected to have received exactly $expected, but received $received"*/)
}

public fun assertHasReceivedExactly(expected: List<Message>, tryReceive: () -> Message?) {
    val received = buildList {
        for (index in 0..<expected.size) {
            add(tryReceive() ?: break)
        }
    }

    assertEquals(expected, received/*, "Expected to have received exactly $expected, but received $received"*/)
}
