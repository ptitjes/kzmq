/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal open class ProtocolError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause)

internal fun protocolError(message: String): Nothing = throw ProtocolError(message)
