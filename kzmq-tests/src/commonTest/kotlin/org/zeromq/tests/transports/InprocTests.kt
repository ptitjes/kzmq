/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.transports

import io.kotest.core.spec.style.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class InprocTests : FunSpec({

    withContext("bind-connect") { ctx ->
        simpleBindConnect(ctx, ctx, randomAddress(Protocol.INPROC))
    }

    withContext("connect-bind") { ctx ->
        simpleConnectBind(ctx, ctx, randomAddress(Protocol.INPROC))
    }
})
