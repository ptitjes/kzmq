/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

internal actual fun addToLoader() {
}

internal actual fun createEngine(coroutineContext: CoroutineContext) =
    CIOEngine(coroutineContext + Dispatchers.IO)

public class CIOEngineContainer : EngineContainer {
    override val factory: EngineFactory = CIO

    override fun toString(): String = CIO.name
}
