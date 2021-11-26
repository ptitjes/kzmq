/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Socket as ZSocket

internal abstract class ZeroMQJsSocket(override val type: Type) : Socket {

    internal abstract val underlying: ZSocket

    override fun close() {
        underlying.close()
    }

    override fun bind(endpoint: String) {
        underlying.bind(endpoint)
    }

    override fun unbind(endpoint: String) {
        underlying.unbind(endpoint)
    }

    override fun connect(endpoint: String) {
        underlying.connect(endpoint)
    }

    override fun disconnect(endpoint: String) {
        underlying.disconnect(endpoint)
    }
}
