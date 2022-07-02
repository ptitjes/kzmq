/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal class InvalidReadFrame(
    override val message: String,
    override val cause: Throwable? = null
) : ProtocolError(message, cause)

internal fun invalidFrame(message: String): Nothing = throw InvalidReadFrame(message)
