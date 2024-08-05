/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.*
import kotlinx.io.bytestring.*

public data class SubscriptionMessage(
    val subscribe: Boolean,
    val topic: ByteString,
)

/**
 * Builds a subscription or unsubscription message.
 */
public fun SubscriptionMessage.toMessage(): Message {
    return Message(Buffer().apply {
        writeByte(if (subscribe) 1 else 0)
        write(topic)
    })
}

/**
 * Destructures a subscription or unsubscription message.
 *
 * @return a pair consisting of a boolean indicating if the message is a subscription message or an unsubscription
 * message, and a topic. Returns `null` if the message is not a subscription/unsubscription message.
 */
public fun Message.toSubscriptionMessage(): SubscriptionMessage? {
    if (isSingle) {
        val bytes = singleOrThrow()
        val firstByte = bytes.readByte().toInt()
        if (firstByte == 0 || firstByte == 1) {
            val subscribe = firstByte == 1
            val topic = bytes.readByteString()
            return SubscriptionMessage(subscribe, topic)
        }
    }
    return null
}
