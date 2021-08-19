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
