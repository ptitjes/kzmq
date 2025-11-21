/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.core.*
import io.kotest.assertions.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.test.*
import org.zeromq.tests.utils.*

@Suppress("unused")
val PullTests by testSuite {

    dualContextTest("SHALL receive incoming messages from its peers using a fair-queuing strategy", config = {
        // TODO investigate why these tests are flaky
        skipAll()
    }) { ctx1, ctx2, protocol ->
        // TODO Investigate why this fails with CIO native
        if (testPlatform.type == TestPlatform.Type.NATIVE) return@dualContextTest

        val address = randomEndpoint(protocol)

        val pushSocketCount = 5
        val pullSocket = ctx2.createPull().apply { bind(address) }
        val pushSockets = List(pushSocketCount) { ctx1.createPush().apply { connect(address) } }

        waitForConnections(pushSocketCount)

        val templates = messages(10) { index ->
            writeFrame(ByteString(index.toByte()))
        }

        pushSockets.forEach { pushSocket ->
            templates.forEach { pushSocket.send(it) }
        }

        all {
            pullSocket shouldReceiveExactly templates.flatMap { template -> List(pushSocketCount) { template } }
        }
    }
}
