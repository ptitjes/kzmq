/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.util.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
internal actual fun addToLoader() {
    Engines.append(CIO)
}

internal actual fun createEngine(coroutineContext: CoroutineContext) =
    CIOEngine(coroutineContext + Dispatchers.IO)

@Suppress("DEPRECATION", "unused")
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val initHook = CIO
