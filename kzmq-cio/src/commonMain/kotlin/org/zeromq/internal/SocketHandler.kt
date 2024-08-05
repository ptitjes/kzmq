/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.coroutines.channels.*
import org.zeromq.*

internal interface SocketHandler {
    suspend fun handle(peerEvents: ReceiveChannel<PeerEvent>)
    suspend fun send(message: Message): Unit = error("Should not be called")
    suspend fun receive(): Message = error("Should not be called")
}
