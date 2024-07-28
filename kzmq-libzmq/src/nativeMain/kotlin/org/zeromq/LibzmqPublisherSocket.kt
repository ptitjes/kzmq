/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.cinterop.*
import org.zeromq.internal.libzmq.*

@OptIn(ExperimentalForeignApi::class)
internal class LibzmqPublisherSocket internal constructor(underlying: COpaquePointer?) :
    LibzmqSocket(underlying, Type.PUB), PublisherSocket {

    override var conflate: Boolean
        by socketOption(underlying, ZMQ_CONFLATE, booleanConverter)

    override var invertMatching: Boolean
        by socketOption(underlying, ZMQ_INVERT_MATCHING, booleanConverter)

    override var noDrop: Boolean
        by socketOption(underlying, ZMQ_XPUB_NODROP, booleanConverter)
}
