/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import java.lang.reflect.*

public actual typealias Closeable = java.io.Closeable

@PublishedApi
internal actual fun Throwable.addSuppressedInternal(other: Throwable) {
    AddSuppressedMethod?.invoke(this, other)
}

private val AddSuppressedMethod: Method? by lazy {
    try {
        Throwable::class.java.getMethod("addSuppressed", Throwable::class.java)
    } catch (t: Throwable) {
        null
    }
}
