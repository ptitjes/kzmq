/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

fun subscriptionMessageOf(subscribe: Boolean, topic: ByteArray): Message {
    val bytes = ByteArray(topic.size + 1) { index ->
        if (index == 0) if (subscribe) 1 else 0
        else topic[index - 1]
    }
    return Message(bytes)
}

fun destructureSubscriptionMessage(message: Message): Pair<Boolean, ByteArray>? {
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
