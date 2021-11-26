/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

object JeroMQ : Engine {
    override val name = "jeromq"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance =
        JeroMQInstance()
}
