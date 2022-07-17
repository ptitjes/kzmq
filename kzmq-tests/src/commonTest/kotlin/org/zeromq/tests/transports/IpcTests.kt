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

private val HELLO = constantFrameOf("Hello 0MQ!")

@Suppress("unused")
class IpcTests : FunSpec({

    withEngines("bind-connect").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.IPC)

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        coroutineScope {
            launch { push.send(messageOf(HELLO)) }
            launch { pull.receive() shouldBe messageOf(HELLO) }
        }
    }

    withEngines("connect-bind").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.IPC)

        val push = ctx1.createPush()
        push.connect(address)

        val pull = ctx2.createPull()
        pull.bind(address)

        coroutineScope {
            launch { push.send(messageOf(HELLO)) }
            launch { pull.receive() shouldBe messageOf(HELLO) }
        }
    }
})
