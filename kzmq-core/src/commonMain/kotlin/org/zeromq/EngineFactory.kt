/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.coroutines.*

/**
 * A ZeroMQ backend engine factory.
 */
public interface EngineFactory {

    /**
     * Returns the unique name of this engine.
     */
    public val name: String

    /**
     * Creates a new [Engine]. Should not be used directly. Use [Context] instead.
     */
    public fun create(coroutineContext: CoroutineContext): Engine
}