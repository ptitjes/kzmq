package org.zeromq.wire

internal class InvalidReadFrame(
    override val message: String,
    override val cause: Throwable? = null
) : ProtocolError(message, cause)

internal fun invalidFrame(message: String): Nothing = throw InvalidReadFrame(message)
