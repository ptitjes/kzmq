/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import org.zeromq.*

internal interface SocketInfo {
    val type: Type
    val validPeerTypes: Set<Type>
    val options: SocketOptions
}
