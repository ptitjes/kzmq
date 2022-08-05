/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import platform.linux.*
import kotlin.coroutines.*
import kotlin.math.*

public actual object CIO : Engine {
    override val name: String = "cio"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance {
        return CIOEngineInstance(coroutineContext + Dispatchers.IO)
    }
}

private val nThreads = max(get_nprocs(), 64)
private val IO_DISPATCHER = newFixedThreadPoolContext(nThreads, "IO")

@Suppress("UnusedReceiverParameter")
private val Dispatchers.IO get() = IO_DISPATCHER
