/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public expect interface Closeable {
    public fun close()
}

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

public inline fun <C : Closeable, R> Pair<C, C>.use(block: (Pair<C, C>) -> R): R {
    return toList().use {
        block(this)
    }
}

@PublishedApi
internal expect fun Throwable.addSuppressedInternal(other: Throwable)
