/*
 * Copyright (c) 2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.util.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
internal actual fun addToLoader() {
    Engines.append(CIO)
}

internal actual fun createEngine(coroutineContext: CoroutineContext): CIOEngine =
    CIOEngine(coroutineContext)

@Suppress("DEPRECATION", "unused")
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val initHook = CIO
