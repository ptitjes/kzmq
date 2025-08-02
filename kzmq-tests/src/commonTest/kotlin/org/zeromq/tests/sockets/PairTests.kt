/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.test.*
import org.zeromq.tests.utils.*

@Suppress("unused")
val PairTests by testSuite {

    withContexts("bind-connect") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val message = message {
            writeFrame("Hello 0MQ!".encodeToByteString())
        }

        val pair1 = ctx1.createPair().apply { bind(address) }
        val pair2 = ctx2.createPair().apply { connect(address) }

        waitForConnections()

        pair1.send(message)
        pair2 shouldReceive message

        pair2.send(message)
        pair1 shouldReceive message
    }

    withContexts("connect-bind") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val message = message {
            writeFrame("Hello 0MQ!".encodeToByteString())
        }

        val pair2 = ctx2.createPair().apply { bind(address) }
        val pair1 = ctx1.createPair().apply { connect(address) }

        waitForConnections()

        pair1.send(message)
        pair2 shouldReceive message

        pair2.send(message)
        pair1 shouldReceive message
    }
}
