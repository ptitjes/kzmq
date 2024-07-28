/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * Iterator for [ReceiveSocket].
 * Instances of this interface are not thread-safe and shall not be used from concurrent coroutines.
 */
public interface SocketIterator {
    /**
     * Returns `true` if the socket has more elements, suspending the caller while this socket is empty,
     * or returns `false` if the socket is closed.
     */
    public suspend operator fun hasNext(): Boolean

    /**
     * Retrieves the element removed from the socket by a preceding call to [hasNext],
     * or throws an [IllegalStateException] if [hasNext] was not invoked.
     *
     * This method should only be used in pair with [hasNext]:
     * ```
     * while (iterator.hasNext()) {
     *   val element = iterator.next()
     *   // ... handle element ...
     * }
     * ```
     * This method throws if the socket is closed.
     */
    public operator fun next(): Message
}
