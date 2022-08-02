/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal data class PeerEvent(
    val kind: Kind,
    val peerMailbox: PeerMailbox,
) {
    internal enum class Kind {
        ADDITION,
        CONNECTION,
        DISCONNECTION,
        REMOVAL,
    }
}
