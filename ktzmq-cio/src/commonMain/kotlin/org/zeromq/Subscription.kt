package org.zeromq

import org.zeromq.internal.*

internal class Subscriptions {
    // TODO Implement this as a byte trie of nodes of type map<PeerMailbox, count>
    private val allSubscriptions = hashMapOf<PeerMailbox, SubscriptionList>()

    fun add(peerMailbox: PeerMailbox, topic: ByteArray) {
        val subscriptions = allSubscriptions.getOrPut(peerMailbox) { SubscriptionList() }
        subscriptions.add(topic)
    }

    fun remove(peerMailbox: PeerMailbox, topic: ByteArray) {
        val subscriptions = allSubscriptions[peerMailbox]
        subscriptions?.remove(topic)
    }

    suspend fun forEachMatching(message: Message, block: suspend (PeerMailbox) -> Unit) {
        val firstPart = message.firstOrThrow()
        for ((peerMailbox, subscriptions) in allSubscriptions) {
            if (subscriptions.hasPrefixOf(firstPart)) block(peerMailbox)
        }
    }
}

private class SubscriptionList {
    private val subscriptions = mutableListOf<Subscription>()

    fun add(topic: ByteArray) {
        subscriptions.add(Subscription(topic))
    }

    fun remove(topic: ByteArray) {
        subscriptions.remove(Subscription(topic))
    }

    fun hasPrefixOf(part: ByteArray): Boolean {
        for (subscription in subscriptions) {
            if (subscription.isPrefixOf(part)) return true
        }
        return false
    }
}

private class Subscription(val topic: ByteArray) {

    fun isPrefixOf(part: ByteArray): Boolean {
        return topic.isPrefixOf(part)
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

private fun ByteArray.isPrefixOf(part: ByteArray): Boolean {
    for ((index, byte) in this.withIndex()) {
        if (part[index] != byte) return false
    }
    return true
}
