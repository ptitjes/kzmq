package org.zeromq

import kotlinx.coroutines.await
import org.zeromq.internal.zeromqjs.Socket as ZSocket

internal abstract class ZeroMQJsSocket(override val type: Type) : Socket {

    internal abstract val underlying: ZSocket

    override fun close() {
        underlying.close()
    }

    override suspend fun bind(endpoint: String) =
        underlying.bind(endpoint).await()

    override suspend fun unbind(endpoint: String) =
        underlying.unbind(endpoint).await()

    override suspend fun connect(endpoint: String) {
        underlying.connect(endpoint)
    }

    override suspend fun disconnect(endpoint: String) {
        underlying.disconnect(endpoint)
    }
}
