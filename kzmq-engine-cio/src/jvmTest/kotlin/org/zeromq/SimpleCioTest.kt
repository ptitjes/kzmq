/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.*
import kotlinx.io.*
import kotlin.test.*

val SimpleCioTest by testSuite {
    test("simple") {
        val ctx1 = Context(CIO)
        val ctx2 = Context(CIO)

        val address = "tcp://localhost:9000"
        val push = ctx1.createPush().apply { connect(address) }
        val pull = ctx2.createPull().apply { bind(address) }

        val messageContent = "Hello"
        push.send { writeFrame { writeString(messageContent) } }
        assertEquals(messageContent, pull.receive { readFrame { readString() } })
    }
}
