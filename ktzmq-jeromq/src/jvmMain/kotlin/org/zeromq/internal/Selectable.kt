/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.atomicfu.*
import org.zeromq.*
import java.nio.channels.*

internal abstract class Selectable {

    abstract val socket: ZMQ.Socket

    abstract val channel: SelectableChannel

    val suspensions = InterestSuspensionsMap()

    private val zinterest = atomic(0)

    val interestOps: Int
        get() {
            var interest = 0
            if (zinterest.value and zmq.ZMQ.ZMQ_POLLIN > 0) {
                interest = interest or SelectionKey.OP_READ
            }
            if (zinterest.value and zmq.ZMQ.ZMQ_POLLOUT > 0) {
                // ZMQ Socket get readiness from the mailbox
                interest = interest or SelectionKey.OP_READ
            }
            return interest
        }

    val readyOps: Int
        get() {
            var ready = 0

            //  The poll item is a 0MQ socket. Retrieve pending events
            //  using the ZMQ_EVENTS socket option.
            val events: Int = socket.base().getSocketOpt(zmq.ZMQ.ZMQ_EVENTS)
            if (events < 0) {
                return -1
            }

            if (zinterest.value and zmq.ZMQ.ZMQ_POLLOUT > 0 && events and zmq.ZMQ.ZMQ_POLLOUT > 0) {
                ready = ready or zmq.ZMQ.ZMQ_POLLOUT
            }
            if (zinterest.value and zmq.ZMQ.ZMQ_POLLIN > 0 && events and zmq.ZMQ.ZMQ_POLLIN > 0) {
                ready = ready or zmq.ZMQ.ZMQ_POLLIN
            }
            return ready
        }

    fun interestOp(interest: SelectInterest, state: Boolean): Int {
        return interestOps(interest.flag, state)
    }

    fun interestOps(ops: Int, state: Boolean): Int {
        while (true) {
            val before = zinterest.value
            val after = if (state) before or ops else before and ops.inv()
            if (zinterest.compareAndSet(before, after)) break
        }
        return zinterest.value
    }

    fun debug(marker: String) {
        trace("$marker > zinterest: ${zinterest.value} interestOps: $interestOps readyOps: $readyOps")
    }

    fun trace(message: String) {
        if (TRACE) println("$socket: $message")
    }
}
