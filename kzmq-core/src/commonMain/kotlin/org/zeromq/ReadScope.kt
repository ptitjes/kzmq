/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.*

public interface ReadScope {

    public fun readFrame(): Buffer

    public fun ignoreRemainingFrames()

    public fun ensureNoRemainingFrames()
}

public inline fun <T> ReadScope.readFrame(reader: Source.() -> T): T {
    val frame = readFrame()
    val result = frame.reader()
    if (!frame.exhausted()) error("Message frame is not exhausted: $frame")
    return result
}
