/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.cinterop.*
import org.zeromq.internal.libzmq.*

internal fun checkNativeError(result: Int) {
    if (result == -1) throwNativeError()
}

@OptIn(ExperimentalForeignApi::class)
internal fun throwNativeError(): Nothing = throw ZeroMQException(ZeroMQError.fromErrno(zmq_errno())!!)
