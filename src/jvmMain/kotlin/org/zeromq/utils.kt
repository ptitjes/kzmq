package org.zeromq

fun <T> wrapException(block: () -> T): T = try {
    block()
} catch (e: ZMQException) {
    throw ZeroMQException(e.errorCode, e)
}
