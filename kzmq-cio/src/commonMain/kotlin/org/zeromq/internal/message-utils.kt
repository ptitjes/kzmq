/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import org.zeromq.*

internal fun addPrefixAddress(message: Message, identities: List<ByteArray> = listOf()): Message =
    Message(identities + ByteArray(0) + message.frames)

internal fun extractPrefixAddress(message: Message): Pair<List<ByteArray>, Message> {
    val delimiterIndex = message.frames.indexOfFirst { it.isEmpty() }
    val identities = message.frames.subList(0, delimiterIndex)
    val data = Message(message.frames.subList(delimiterIndex + 1, message.frames.size))
    return identities to data
}
