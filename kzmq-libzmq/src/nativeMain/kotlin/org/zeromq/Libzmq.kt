/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

public object Libzmq : EngineFactory {
    override val name: String = "libzmq"
    override fun create(coroutineContext: CoroutineContext): Engine = LibzmqEngine()
}
