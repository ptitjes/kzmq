/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.test.*
import kotlin.test.*

class SimpleTest {
    @Test
    fun testSimple() = runTest {
        val ctx1 = Context(JeroMQ)
        val ctx2 = Context(JeroMQ)

        val address = "tcp://localhost:9000"
        val push = ctx1.createPush().apply { bind(address) }
        val pull = ctx2.createPull().apply { connect(address) }

        val messageContent = "Hello"
        push.send(Message(messageContent.encodeToByteArray()))
        assertEquals(messageContent, pull.receive().frames.getOrNull(0)?.decodeToString())
    }
}
