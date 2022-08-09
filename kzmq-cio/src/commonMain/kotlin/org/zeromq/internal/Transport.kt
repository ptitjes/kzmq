/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.coroutines.*

internal interface Transport {
    fun supportsSchemes(scheme: String): Boolean
    val isMulticast: Boolean

    fun close()

    fun bind(
        mainScope: CoroutineScope,
        lingerScope: CoroutineScope,
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        address: String,
    ): BindingHolder

    fun connect(
        mainScope: CoroutineScope,
        lingerScope: CoroutineScope,
        peerManager: PeerManager,
        socketInfo: SocketInfo,
        address: String,
    ): ConnectionHolder
}

internal interface BindingHolder {
    fun close()
}

internal interface ConnectionHolder {
    fun close()
}
