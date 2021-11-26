/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import java.net.*

actual fun findOpenPort(): Int =
    ServerSocket(0, 0).use { tmpSocket -> return tmpSocket.localPort }
