/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import org.zeromq.*
import org.zeromq.test.*

suspend infix fun ReceiveSocket.shouldReceive(expected: MessageTemplate) =
    shouldReceiveExactly(listOf(expected)) { receive() }

suspend infix fun ReceiveSocket.shouldReceiveExactly(expected: List<MessageTemplate>) =
    shouldReceiveExactly(expected) { receive() }
