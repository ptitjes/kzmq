/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.util.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
public object ZeroMQjs : EngineFactory {
    override val name: String = "ZeroMQ.js"
    override fun create(coroutineContext: CoroutineContext): Engine = ZeroMQjsEngine()

    init {
        Engines.append(ZeroMQjs)
    }
}

@Suppress("DEPRECATION", "unused")
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val initHook = ZeroMQjs
