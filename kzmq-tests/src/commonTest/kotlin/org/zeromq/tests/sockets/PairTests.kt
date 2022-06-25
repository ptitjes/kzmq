/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class PairTests : FunSpec({

    withEngines("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val pair1 = ctx1.createPair()
        pair1.bind(address)

        val pair2 = ctx2.createPair()
        pair2.connect(address)

        pair1.send(message)
        pair2.receive() shouldBe message

        pair2.send(message)
        pair1.receive() shouldBe message
    }

    withEngines("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val pair1 = ctx1.createPair()
        pair1.connect(address)

        val pair2 = ctx2.createPair()
        pair2.bind(address)

        pair1.send(message)
        pair2.receive() shouldBe message

        pair2.send(message)
        pair1.receive() shouldBe message
    }
})
