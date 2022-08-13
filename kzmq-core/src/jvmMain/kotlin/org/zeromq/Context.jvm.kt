/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.*

public actual fun CoroutineScope.Context(
    additionalContext: CoroutineContext,
): Context = Context(FACTORY, additionalContext)

/**
 * A container is searched across dependencies using [ServiceLoader] to find client implementations.
 * An implementation of this interface provides a ZeroMQ engine [factory] and is only used to find
 * the default engine when [Context] function is called with no particular engine specified.
 *
 * @property factory that produces engine instances
 */
public interface EngineContainer {
    public val factory: EngineFactory
}

/**
 * Workaround for dummy android [ClassLoader].
 */
private val engines: List<EngineContainer> = EngineContainer::class.java.let {
    ServiceLoader.load(it, it.classLoader).toList()
}

private val FACTORY = engines.firstOrNull()?.factory ?: error(
    "Failed to find ZeroMQ engine implementation in the classpath: consider adding engine dependency. " +
        "See https://github.com/ptitjes/kzmq#gradle"
)
