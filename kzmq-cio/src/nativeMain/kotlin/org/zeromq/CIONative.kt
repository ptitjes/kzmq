/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import org.zeromq.util.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
public actual object CIO : EngineFactory {
    override val name: String = "cio"
    override fun create(coroutineContext: CoroutineContext): Engine {
        return CIOEngine(coroutineContext + Dispatchers.Default)
    }

    init {
        Engines.append(CIO)
    }
}

@Suppress("DEPRECATION", "unused")
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val initHook = CIO
