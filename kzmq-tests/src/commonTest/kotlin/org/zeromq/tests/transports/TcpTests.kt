/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.transports

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class TcpTests : FunSpec({

    withEngines("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.TCP)
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        coroutineScope {
            launch { push.send(message) }
            launch { pull.receive() shouldBe message }
        }
    }

    withEngines("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.TCP)
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.connect(address)

        val pull = ctx2.createPull()
        pull.bind(address)

        coroutineScope {
            launch { push.send(message) }
            launch { pull.receive() shouldBe message }
        }
    }
})
