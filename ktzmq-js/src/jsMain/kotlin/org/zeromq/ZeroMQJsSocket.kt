package org.zeromq

import kotlinx.coroutines.await
import org.zeromq.internal.zeromqjs.Socket as ZSocket

internal abstract class ZeroMQJsSocket : Socket {

    internal abstract val underlying: ZSocket

    override fun close() {
        underlying.close()
    }

    override suspend fun bind(endpoint: String) =
        underlying.bind(endpoint).await()

    override fun connect(endpoint: String) {
        underlying.connect(endpoint)
    }

    override fun disconnect(endpoint: String) {
        underlying.disconnect(endpoint)
    }
}
