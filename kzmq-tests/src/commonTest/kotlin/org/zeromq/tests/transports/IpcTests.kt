/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.transports

import io.kotest.core.spec.style.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class IpcTests : FunSpec({

    withContexts("bind-connect").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        simpleBindConnect(ctx1, ctx2, randomAddress(Protocol.IPC))
    }

    withContexts("connect-bind").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        simpleConnectBind(ctx1, ctx2, randomAddress(Protocol.IPC))
    }
})
