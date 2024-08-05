/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*

internal fun Message.popIdentity(): Identity {
    return Identity(readFrame().readByteString())
}

internal fun Message.pushIdentity(identity: Identity) {
    writeFrames(listOf(Buffer().apply { write(identity.value) }) + readFrames())
}

internal fun Message.popPrefixAddress(): List<ByteString> {
    val frames = readFrames()
    val delimiterIndex = frames.indexOfFirst { it.exhausted() }
    val identities = frames.subList(0, delimiterIndex).map { it.readByteString() }
    this.writeFrames(frames.subList(delimiterIndex + 1, frames.size))
    return identities
}

internal fun Message.pushPrefixAddress(identities: List<ByteString> = listOf()) {
    writeFrames(identities.map { Buffer().apply { write(it) } } + Buffer() + readFrames())
}
