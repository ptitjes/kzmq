/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import zmq.ZMQ.*

internal enum class SelectInterest(val flag: Int) {
    READ(ZMQ_POLLIN),
    WRITE(ZMQ_POLLOUT),
    ERROR(ZMQ_POLLERR);

    companion object {
        val AllInterests: Array<SelectInterest> = values()
    }
}
