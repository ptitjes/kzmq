/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal data class SubscriptionTrie<T>(
    val subscriptions: Map<T, Int> = hashMapOf(),
    val children: Map<Byte, SubscriptionTrie<T>> = hashMapOf(),
) {
    fun add(topic: ByteArray, element: T): SubscriptionTrie<T> = this.add(topic.iterator(), element)

    private fun add(bytes: ByteIterator, element: T): SubscriptionTrie<T> = if (bytes.hasNext()) {
        val byte = bytes.nextByte()
        val newChild = (children[byte] ?: SubscriptionTrie()).add(bytes, element)
        this.copy(children = children + (byte to newChild))
    } else {
        val newCount = (subscriptions[element] ?: 0) + 1
        this.copy(subscriptions = subscriptions + (element to newCount))
    }

    fun remove(topic: ByteArray, element: T): SubscriptionTrie<T> = this.remove(topic.iterator(), element)

    private fun remove(bytes: ByteIterator, element: T): SubscriptionTrie<T> = if (bytes.hasNext()) {
        val byte = bytes.nextByte()
        val newChild = (children[byte] ?: SubscriptionTrie()).remove(bytes, element)
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

    suspend fun forEachMatching(topic: ByteArray, block: suspend (T) -> Unit) {
        forEachMatching(topic.iterator(), mutableSetOf(), block)
    }

    private suspend fun forEachMatching(
        bytes: ByteIterator,
        alreadyVisited: MutableSet<T>,
        block: suspend (T) -> Unit,
    ) {
        for (mailbox in subscriptions.keys) {
            if (!alreadyVisited.contains(mailbox)) {
                block(mailbox)
                alreadyVisited += mailbox
            }
        }

        if (bytes.hasNext()) {
            val child = children[bytes.nextByte()] ?: return
            child.forEachMatching(bytes, alreadyVisited, block)
        }
    }
}
