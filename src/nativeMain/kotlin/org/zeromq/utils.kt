package org.zeromq

import libzmq.EFSM
import libzmq.EMTHREAD
import libzmq.ENOCOMPATPROTO
import libzmq.ETERM
import platform.posix.*

internal fun checkNativeError(result: Int) {
    if (result == -1) throwNativeError()
}

internal fun throwNativeError(): Nothing = throw ZeroMQException(errno)
