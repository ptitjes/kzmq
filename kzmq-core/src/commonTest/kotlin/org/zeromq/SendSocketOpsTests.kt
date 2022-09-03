/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.core.spec.style.*
import kotlinx.coroutines.flow.*
import org.kodein.mock.*

@UsesMocks(SendSocket::class)
class SendSocketOpsTests : FunSpec({
    test("collectToSocket") {
        with(Mocker()) {
            val socket = MockSendSocket(this)

            everySuspending { socket.send(isAny()) } returns Unit

            val messages = List(10) { Message("message-$it".encodeToByteArray()) }

            messages.asFlow().collectToSocket(socket)

            verifyWithSuspend {
                messages.forEach { message ->
                    socket.send(message)
                }
            }
        }
    }
})
