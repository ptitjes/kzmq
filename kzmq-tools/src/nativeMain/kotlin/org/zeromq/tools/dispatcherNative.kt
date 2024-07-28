/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tools

import kotlinx.coroutines.*
import platform.linux.*
import kotlin.math.*

private val nThreads = max(get_nprocs(), 64)
private val IO_DISPATCHER = newFixedThreadPoolContext(nThreads, "IO")

actual val dispatcher: CoroutineDispatcher = IO_DISPATCHER
