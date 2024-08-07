/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.coroutines.channels.*
import kotlinx.io.bytestring.*
import kotlin.jvm.*
import kotlin.random.*

internal class PeerMailbox(val endpoint: String, socketOptions: SocketOptions) {
    val receiveChannel = Channel<CommandOrMessage>(socketOptions.receiveQueueSize)
    val sendChannel = Channel<CommandOrMessage>(socketOptions.sendQueueSize)

    internal var identity: Identity? = null

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

@JvmInline
internal value class Identity(val value: ByteString) {
    companion object {
        fun random() = Identity(ByteString(Random.nextBytes(ByteArray(16))))
    }
}
