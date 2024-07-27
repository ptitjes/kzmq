/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

public object CIO : EngineFactory {
    init {
        addToLoader()
    }

    override val name: String = "CIO"
    override val supportedTransports: Set<String> = setOf("inproc", "tcp", "ipc")
    override fun create(coroutineContext: CoroutineContext): Engine = createEngine(coroutineContext)
}

internal expect fun addToLoader()

internal expect fun createEngine(coroutineContext: CoroutineContext): CIOEngine
