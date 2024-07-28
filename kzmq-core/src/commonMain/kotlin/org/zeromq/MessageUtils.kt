/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * Builds a subscription or unsubscription message.
 *
 * @param subscribe `true` if the message is a subscription message, false if it is an unsubscription message.
 * @param topic the topic to subscription/unsubscription message.
 */
public fun subscriptionMessageOf(subscribe: Boolean, topic: ByteArray): Message {
    val bytes = ByteArray(topic.size + 1) { index ->
        if (index == 0) if (subscribe) 1 else 0
        else topic[index - 1]
    }
    return Message(bytes)
}

/**
 * Destructures a subscription or unsubscription message.
 *
 * @return a pair consisting of a boolean indicating if the message is a subscription message or an unsubscription
 * message, and a topic. Returns `null` if the message is not a subscription/unsubscription message.
 */
public fun destructureSubscriptionMessage(message: Message): Pair<Boolean, ByteArray>? {
    if (message.isSingle) {
        val bytes = message.singleOrThrow()
        val firstByte = bytes[0].toInt()
        if (firstByte == 0 || firstByte == 1) {
            val subscribe = firstByte == 1
            val topic = bytes.sliceArray(1 until bytes.size)
            return subscribe to topic
        }
    }
    return null
}
