/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.inproc

import org.zeromq.internal.*

internal interface InprocEndpointEvent {
    data class Binding(
        val peerManager: PeerManager,
        val routingId: Identity?,
        val mailboxFactory: () -> PeerMailbox,
    ) : InprocEndpointEvent

    object Unbinding : InprocEndpointEvent

    data class Connecting(
        val mailbox: PeerMailbox,
        val peerManager: PeerManager,
        val routingId: Identity?,
    ) : InprocEndpointEvent

    data class Disconnecting(
        val mailbox: PeerMailbox,
    ) : InprocEndpointEvent
}
