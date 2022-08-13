/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.zeromq.util.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
public actual fun CoroutineScope.Context(
    additionalContext: CoroutineContext,
): Context = engines.firstOrNull()?.let { Context(it, additionalContext) } ?: error(
    "Failed to find ZeroMQ engine implementation in the classpath: consider adding engine dependency. " +
        "See https://github.com/ptitjes/kzmq#gradle"
)

/**
 * Shared engines collection.
 * Use [engines.append] to enable engine auto discover in [Context].
 */
@Suppress("ClassName")
@InternalAPI
public object engines : Iterable<EngineFactory> {
    private val head = atomic<Node?>(null)

    /**
     * Add engine to head.
     */
    public fun append(item: EngineFactory) {
        while (true) {
            val current = head.value
            val new = Node(item, current)

            if (head.compareAndSet(current, new)) break
        }
    }

    /**
     * @return engine collection iterator.
     */
    override fun iterator(): Iterator<EngineFactory> = object : Iterator<EngineFactory> {
        var current = head.value

        override fun next(): EngineFactory {
            val result = current!!
            current = result.next
            return result.item
        }

        override fun hasNext(): Boolean = (null != current)
    }

    private class Node(
        val item: EngineFactory,
        val next: Node?,
    )
}
