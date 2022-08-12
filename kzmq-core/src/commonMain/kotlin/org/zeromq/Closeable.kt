/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A resource that must manually be closed by calling its [close] method..
 */
public expect interface Closeable {
    /**
     * Closes this resource.
     */
    public fun close()
}

/**
 * Automatically closes the receiver [Closeable] after executing the specified [block].
 *
 * @param block the block to execute in the context of the receiver [Closeable].
 */
public inline fun <C : Closeable, R> C.use(block: (C) -> R): R {
    var closed = false

    return try {
        block(this)
    } catch (first: Throwable) {
        try {
            closed = true
            close()
        } catch (second: Throwable) {
            first.addSuppressedInternal(second)
        }

        throw first
    } finally {
        if (!closed) {
            close()
        }
    }
}

/**
 * Automatically closes each element of the receiver collection of [Closeable] after executing the specified [block].
 *
 * @param block the block to execute in the context of the receiver [Closeable] collection.
 */
public inline fun <C : Closeable, CS : Collection<C>, R> CS.use(block: (CS) -> R): R {
    var closed = false

    return try {
        block(this)
    } catch (first: Throwable) {
        closed = true
        forEach {
            try {
                it.close()
            } catch (second: Throwable) {
                first.addSuppressedInternal(second)
            }
        }

        throw first
    } finally {
        if (!closed) {
            forEach { it.close() }
        }
    }
}

/**
 * Automatically closes a pair of [Closeable] after executing the specified [block].
 *
 * @param block the block to execute in the context of the receiver [Closeable] pair.
 */
public inline fun <C : Closeable, R> Pair<C, C>.use(block: (Pair<C, C>) -> R): R {
    return toList().use {
        block(this)
    }
}

@PublishedApi
internal expect fun Throwable.addSuppressedInternal(other: Throwable)
