package org.zeromq

import kotlinx.cinterop.COpaquePointer
import org.zeromq.internal.libzmq.ZMQ_CONFLATE
import org.zeromq.internal.libzmq.ZMQ_INVERT_MATCHING

internal class LibzmqSubscriberSocket internal constructor(underlying: COpaquePointer?) :
    LibzmqSocket(underlying), SubscriberSocket {

    override var conflate: Boolean
            by socketOption(underlying, ZMQ_CONFLATE, booleanConverter)

    override var invertMatching: Boolean
            by socketOption(underlying, ZMQ_INVERT_MATCHING, booleanConverter)
}
