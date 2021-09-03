package org.zeromq.internal

internal open class ProtocolError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause)

fun protocolError(message: String): Nothing = throw ProtocolError(message)
