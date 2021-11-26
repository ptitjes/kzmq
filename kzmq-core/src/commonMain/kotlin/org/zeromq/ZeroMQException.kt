/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

class ZeroMQException(val error: ZeroMQError, cause: Throwable? = null) :
    RuntimeException(error.message, cause) {

    constructor(errno: Int, cause: Throwable? = null) : this(
        ZeroMQError.fromErrno(errno) ?: throw IllegalArgumentException("Unknown error code $errno"),
        cause
    )
}

private const val ZMQ_HAUSNUMERO = 156384712

enum class ZeroMQError(val errno: Int, val message: String) {
    ENOENT(2, "The provided endpoint is not connected"),
    EINTR(4, "The operation was interrupted by delivery of a signal before the message was sent"),
    EACCESS(13, ""),
    EFAULT(14, ""),
    EINVAL(22, "The endpoint supplied is invalid"),
    EAGAIN(
        35,
        "Non-blocking mode was requested and the message cannot be sent or received at the moment"
    ),
    EINPROGRESS(36, ""),
    EPROTONOSUPPORT(43, "The requested transport protocol is not supported"),
    ENOTSUP(45, "The send operation is not supported by this socket type"),
    EADDRINUSE(48, ""),
    EADDRNOTAVAIL(49, ""),
    ENETDOWN(50, ""),
    ENOBUFS(55, ""),
    EISCONN(56, ""),
    ENOTCONN(57, ""),
    ECONNREFUSED(61, ""),
    EHOSTUNREACH(65, "The message cannot be routed"),

    ENOTSOCK(ZMQ_HAUSNUMERO + 5, "The provided socket was invalid"),
    EMSGSIZE(ZMQ_HAUSNUMERO + 10, ""),
    EAFNOSUPPORT(ZMQ_HAUSNUMERO + 11, ""),
    ENETUNREACH(ZMQ_HAUSNUMERO + 12, ""),

    ECONNABORTED(ZMQ_HAUSNUMERO + 13, ""),
    ECONNRESET(ZMQ_HAUSNUMERO + 14, ""),
    ETIMEDOUT(ZMQ_HAUSNUMERO + 16, ""),
    ENETRESET(ZMQ_HAUSNUMERO + 18, ""),

    EFSM(
        ZMQ_HAUSNUMERO + 51,
        "The send operation cannot be performed on this socket at the moment due to the socket not being in the appropriate state"
    ),
    ENOCOMPATPROTO(
        ZMQ_HAUSNUMERO + 52,
        "The requested transport protocol is not compatible with the socket type"
    ),
    ETERM(
        ZMQ_HAUSNUMERO + 53,
        "The ØMQ context associated with the specified socket was terminated"
    ),
    EMTHREAD(ZMQ_HAUSNUMERO + 54, "No I/O thread is available to accomplish the task"),

    EIOEXC(ZMQ_HAUSNUMERO + 105, ""),
    ESOCKET(ZMQ_HAUSNUMERO + 106, ""),
    EMFILE(ZMQ_HAUSNUMERO + 107, ""),

    EPROTO(ZMQ_HAUSNUMERO + 108, ""),

    ;

    companion object {
        private val errnoToError = values().associateBy { e -> e.errno }

        fun fromErrno(errno: Int) = errnoToError[errno]
    }
}
