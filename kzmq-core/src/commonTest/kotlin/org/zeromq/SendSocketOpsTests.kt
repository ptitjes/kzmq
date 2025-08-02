/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import dev.mokkery.*
import dev.mokkery.answering.*
import dev.mokkery.matcher.*
import kotlinx.coroutines.flow.*

val SendSocketOpsTests by testSuite {
    test("collectToSocket") {
        val socket = mock<SendSocket> {
            everySuspend { send(any()) } returns Unit
        }
        val messages = List(10) { Message("message-$it") }

        messages.asFlow().collectToSocket(socket)

        verifySuspend {
            messages.forEach { message ->
                socket.send(message)
            }
        }
    }
}
