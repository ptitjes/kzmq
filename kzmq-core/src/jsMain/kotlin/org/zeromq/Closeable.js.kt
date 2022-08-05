/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public actual interface Closeable {
    public actual fun close()
}

@PublishedApi
internal actual fun Throwable.addSuppressedInternal(other: Throwable) {
}
