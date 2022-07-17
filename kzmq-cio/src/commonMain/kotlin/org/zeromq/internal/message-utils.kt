/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import org.zeromq.*

internal fun Message.addPrefixAddresses(identities: List<Frame>) {
    pushFirst(Frame.EMPTY)
    identities.asReversed().forEach { pushFirst(it) }
}

internal fun Message.removePrefixAddresses(): List<Frame> {
    return removeFirstWhile { it.isNotEmpty() }.also {
        removeFirst()
    }
}
