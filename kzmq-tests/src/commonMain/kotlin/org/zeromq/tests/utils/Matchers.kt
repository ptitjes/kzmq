/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import org.zeromq.*
import org.zeromq.test.*

suspend fun assertReceivesExactly(expected: List<Message>, socket: ReceiveSocket) {
    assertReceivesExactly(expected) { socket.receive() }
}
