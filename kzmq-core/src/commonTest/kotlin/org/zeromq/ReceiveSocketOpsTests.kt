/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import dev.mokkery.*
import dev.mokkery.answering.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.flow.*

val ReceiveSocketOpsTests by testSuite {
    test("consumeAsFlow") {
        val messages = List(10) { Message("message-$it") }

        val socket = mock<ReceiveSocket> {
            val messageIterator = messages.iterator()
            everySuspend { receive() } calls { messageIterator.next() }
        }

        socket.consumeAsFlow().take(10).toList() shouldContainExactly messages
    }
}
