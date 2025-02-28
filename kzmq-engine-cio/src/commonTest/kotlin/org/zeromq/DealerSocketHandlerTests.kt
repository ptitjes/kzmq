/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.core.spec.style.*
import org.zeromq.fragments.*

internal class DealerSocketHandlerTests : FunSpec({
    val factory = ::DealerSocketHandler

    suspendingSendTests(factory)
    suspendingReceiveTests(factory)
})
