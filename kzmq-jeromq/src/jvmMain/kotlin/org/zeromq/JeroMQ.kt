/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

public object JeroMQ : EngineFactory {
    override val name: String = "JeroMQ"
    override val supportedTransports: Set<String> = setOf("inproc", "tcp")
    override fun create(coroutineContext: CoroutineContext): Engine = JeroMQEngine()
}

public class JeroMQEngineContainer : EngineContainer {
    override val factory: EngineFactory = JeroMQ

    override fun toString(): String = JeroMQ.name
}
