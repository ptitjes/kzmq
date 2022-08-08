/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import kotlinx.coroutines.*

internal suspend fun waitForSubscriptions() = delay(200)

internal fun Int.packToByteArray() = ByteArray(1) { toByte() }
