/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.util.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
public object JS : EngineFactory {
    override val name: String = "zeromq.js"
    override fun create(coroutineContext: CoroutineContext): Engine = JSEngine()

    init {
        engines.append(JS)
    }
}

@Suppress("DEPRECATION", "unused")
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val initHook = JS
