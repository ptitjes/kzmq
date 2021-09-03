package org.zeromq.internal

import kotlinx.coroutines.channels.*
import org.zeromq.*

internal class PeerMailbox(private val endpoint: Endpoint, socketOptions: SocketOptions) {
    val receiveChannel = Channel<CommandOrMessage>(socketOptions.receiveQueueSize)
    val sendChannel = Channel<CommandOrMessage>(socketOptions.sendQueueSize)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PeerMailbox

        if (endpoint != other.endpoint) return false

        return true
    }

    override fun hashCode(): Int {
        return endpoint.hashCode()
    }

    override fun toString(): String {
        return "PeerMailbox(endpoint=$endpoint)"
    }
}
