/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import platform.posix.*

internal fun checkNativeError(result: Int) {
    if (result == -1) throwNativeError()
}

internal fun throwNativeError(): Nothing = throw ZeroMQException(errno)
