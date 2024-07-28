/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.atomicfu.*
import org.zeromq.util.*

/**
 * Shared engines collection.
 * Use [Engines.append] to enable engine auto discover in [Context].
 */
@InternalAPI
public object Engines : Iterable<EngineFactory> {
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

@OptIn(InternalAPI::class)
public actual val engines: List<EngineFactory> get() = Engines.toList()
