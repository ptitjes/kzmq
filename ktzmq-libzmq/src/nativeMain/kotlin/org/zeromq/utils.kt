package org.zeromq

import platform.posix.errno

internal fun checkNativeError(result: Int) {
    if (result == -1) throwNativeError()
}

internal fun throwNativeError(): Nothing = throw ZeroMQException(errno)
