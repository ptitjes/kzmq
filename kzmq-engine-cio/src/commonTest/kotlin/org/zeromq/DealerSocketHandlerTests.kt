/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import io.kotest.assertions.*
import org.zeromq.fragments.*
import org.zeromq.internal.*
import org.zeromq.test.*
import org.zeromq.utils.*

internal class DealerSocketHandlerTests : SocketHandlerTests(::DealerSocketHandler, {
    suspendingSendTests()
})
