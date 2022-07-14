/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import org.zeromq.*

internal fun addPrefixAddress(message: Message, identities: List<ByteArray> = listOf()): Message =
    Message(identities + ByteArray(0) + message.parts)

internal fun extractPrefixAddress(message: Message): Pair<List<ByteArray>, Message> {
    val delimiterIndex = message.parts.indexOfFirst { it.isEmpty() }
    val identities = message.parts.subList(0, delimiterIndex)
    val data = Message(message.parts.subList(delimiterIndex + 1, message.parts.size))
    return identities to data
}
