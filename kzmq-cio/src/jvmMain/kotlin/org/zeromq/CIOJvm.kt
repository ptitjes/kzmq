/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

public actual object CIO : EngineFactory {
    override val name: String = "CIO"
    override val supportedTransports: Set<String> = setOf("inproc", "tcp", "ipc")
    override fun create(coroutineContext: CoroutineContext): Engine =
        CIOEngine(coroutineContext + Dispatchers.IO)
}

public class CIOEngineContainer : EngineContainer {
    override val factory: EngineFactory = CIO

    override fun toString(): String = CIO.name
}
