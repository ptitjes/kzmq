/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import dev.mokkery.*
import dev.mokkery.answering.*
import dev.mokkery.matcher.*
import io.kotest.core.spec.style.*
import kotlinx.coroutines.flow.*

class SendSocketOpsTests : FunSpec({
    test("collectToSocket") {
        val socket = mock<SendSocket> {
            everySuspend { send(any()) } returns Unit
        }
        val messages = List(10) { Message("message-$it".encodeToByteArray()) }

        messages.asFlow().collectToSocket(socket)

        verifySuspend {
            messages.forEach { message ->
                socket.send(message)
            }
        }
    }
})
