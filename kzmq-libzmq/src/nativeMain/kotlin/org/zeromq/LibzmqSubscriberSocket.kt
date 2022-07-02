/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.cinterop.*
import org.zeromq.internal.libzmq.ZMQ_CONFLATE
import org.zeromq.internal.libzmq.ZMQ_INVERT_MATCHING

internal class LibzmqSubscriberSocket internal constructor(underlying: COpaquePointer?) :
    LibzmqSocket(underlying, Type.SUB), SubscriberSocket {

    override var conflate: Boolean
            by socketOption(underlying, ZMQ_CONFLATE, booleanConverter)

    override var invertMatching: Boolean
            by socketOption(underlying, ZMQ_INVERT_MATCHING, booleanConverter)
}
