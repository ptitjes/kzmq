/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * Represents a subscription trie.
 *
 * The subscription is a binary string that specifies what messages the subscriber wants. A
 * subscription of “A” SHALL match all messages starting with “A”. An empty subscription SHALL
 * match all messages.
 *
 * Subscriptions SHALL be additive and SHALL NOT be idempotent. That is, subscribing to “A” and "”
 * is the same as subscribing to "” alone. Subscribing to “A” and “A” counts as two subscriptions,
 * and would require two CANCEL commands to undo.
 */
internal data class SubscriptionTrie<T>(
    val subscriptions: Map<T, Int> = hashMapOf(),
    val children: Map<Byte, SubscriptionTrie<T>> = hashMapOf(),
) {
    fun add(prefix: ByteArray, element: T): SubscriptionTrie<T> = this.add(prefix.iterator(), element)

    private fun add(prefix: ByteIterator, element: T): SubscriptionTrie<T> = if (prefix.hasNext()) {
        val byte = prefix.nextByte()
        val newChild = (children[byte] ?: SubscriptionTrie()).add(prefix, element)
        this.copy(children = children + (byte to newChild))
    } else {
        val newCount = (subscriptions[element] ?: 0) + 1
        this.copy(subscriptions = subscriptions + (element to newCount))
    }

    fun remove(prefix: ByteArray, element: T): SubscriptionTrie<T> = this.remove(prefix.iterator(), element)

    private fun remove(prefix: ByteIterator, element: T): SubscriptionTrie<T> = if (prefix.hasNext()) {
        val byte = prefix.nextByte()
        val newChild = (children[byte] ?: SubscriptionTrie()).remove(prefix, element)
        val newChildren = children + (byte to newChild)
        this.copy(children = newChildren)
    } else {
        val newCount = (subscriptions[element] ?: 0) - 1
        when {
            newCount < 0 -> this
            newCount == 0 -> this.copy(subscriptions = subscriptions - element)
            else -> this.copy(subscriptions = subscriptions + (element to newCount))
        }
    }

    suspend fun forEachMatching(content: ByteArray, block: suspend (T) -> Unit) {
        forEachMatching(content.iterator(), mutableSetOf(), block)
    }

    private suspend fun forEachMatching(
        content: ByteIterator,
        alreadyVisited: MutableSet<T>,
        block: suspend (T) -> Unit,
    ) {
        for (mailbox in subscriptions.keys) {
            if (!alreadyVisited.contains(mailbox)) {
                block(mailbox)
                alreadyVisited += mailbox
            }
        }

        if (content.hasNext()) {
            val child = children[content.nextByte()] ?: return
            child.forEachMatching(content, alreadyVisited, block)
        }
    }
}
