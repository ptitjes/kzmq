/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.util.*
import platform.linux.*
import kotlin.coroutines.*
import kotlin.math.*

@OptIn(InternalAPI::class)
public actual object CIO : EngineFactory {
    override val name: String = "cio"
    override fun create(coroutineContext: CoroutineContext): Engine {
        return CIOEngine(coroutineContext + IO_DISPATCHER)
    }

    init {
        engines.append(CIO)
    }
}

private val nThreads = 1
private val IO_DISPATCHER = newFixedThreadPoolContext(nThreads, "IO")

@Suppress("DEPRECATION", "unused")
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val initHook = CIO
