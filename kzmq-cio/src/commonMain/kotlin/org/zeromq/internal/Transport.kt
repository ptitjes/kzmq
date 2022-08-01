/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal interface Transport {
    fun supportsSchemes(scheme: String): Boolean
    val isMulticast: Boolean

    fun close()

    suspend fun bind(peerManager: PeerManager, socketInfo: SocketInfo, endpoint: String)
    suspend fun connect(peerManager: PeerManager, socketInfo: SocketInfo, endpoint: String)
}
