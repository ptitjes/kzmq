/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

internal suspend fun waitForConnections(count: Int = 1) = delay(100.milliseconds * count)

internal suspend fun waitForSubscriptions(count: Int = 1) = delay(200.milliseconds * count)
