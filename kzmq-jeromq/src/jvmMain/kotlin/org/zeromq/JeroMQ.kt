/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

public object JeroMQ : Engine {
    override val name: String = "jeromq"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance =
        JeroMQInstance()
}
