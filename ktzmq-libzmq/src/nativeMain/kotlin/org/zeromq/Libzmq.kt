/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

object Libzmq : Engine {
    override val name = "libzmq"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance = LibzmqInstance()
}
