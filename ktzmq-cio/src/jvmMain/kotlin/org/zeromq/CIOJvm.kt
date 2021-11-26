/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

actual object CIO : Engine {
    override val name = "cio"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance =
        CIOInstance(coroutineContext + Dispatchers.IO)
}
