/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.*
import kotlinx.io.bytestring.*

public interface WriteScope {

    public fun writeFrame(source: Buffer)
}

public inline fun WriteScope.writeFrame(writer: Sink.() -> Unit) {
    val frame = Buffer()
    frame.writer()
    writeFrame(frame)
}

public fun WriteScope.writeEmptyFrame() {
    writeFrame(ByteString())
}

public fun WriteScope.writeFrame(byteString: ByteString) {
    writeFrame { write(byteString) }
}

public fun WriteScope.writeFrame(string: String) {
    writeFrame { writeString(string) }
}
