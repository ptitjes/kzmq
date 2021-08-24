package org.zeromq

internal class Subscription(val topic: ByteArray) {

    fun isMatchedBy(messagePart: ByteArray): Boolean {
        for ((index, byte) in topic.withIndex()) {
            if (messagePart[index] != byte) return false
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Subscription
        if (!topic.contentEquals(other.topic)) return false
        return true
    }

    override fun hashCode(): Int {
        return topic.contentHashCode()
    }

    override fun toString(): String {
        return "Subscription(topic=${topic.contentToString()})"
    }
}
