/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.core.spec.style.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.flow.*
import org.kodein.mock.*

@UsesMocks(ReceiveSocket::class)
class ReceiveSocketOpsTests : FunSpec({
    test("consumeAsFlow") {
        with(Mocker()) {
            val socket = MockReceiveSocket(this)

            val messages = List(10) { Message("message-$it".encodeToByteArray()) }

            val messageIterator = messages.iterator()
            everySuspending { socket.receive() } runs { messageIterator.next() }

            socket.consumeAsFlow().take(10).toList() shouldContainExactly messages
        }
    }
})
