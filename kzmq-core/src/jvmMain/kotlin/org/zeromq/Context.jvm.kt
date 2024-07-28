/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import java.util.*

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
private val engineContainers: List<EngineContainer> = EngineContainer::class.java.let {
    ServiceLoader.load(it, it.classLoader).toList()
}

public actual val engines: List<EngineFactory> = engineContainers.map { it.factory }
