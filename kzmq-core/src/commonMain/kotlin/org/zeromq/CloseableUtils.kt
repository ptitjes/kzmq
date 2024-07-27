/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public inline fun <R> use(vararg closeables: AutoCloseable, block: () -> R): R {
    try {
        return block()
    } finally {
        closeables.forEach { it.close() }
    }
}
