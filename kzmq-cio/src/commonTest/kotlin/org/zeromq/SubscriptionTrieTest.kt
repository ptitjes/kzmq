/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.test.*
import kotlin.test.*

internal class SubscriptionTrieTest {

    @Test
    fun `doesn't match null subscription`() = subscriptionTest("any", setOf<Int>()) { it }

    @Test
    fun `matches empty subscription`() = subscriptionTest("any", setOf(1)) {
        it.add("", 1)
    }

    @Test
    fun `matches only prefixes`() = subscriptionTest("a", setOf(1)) {
        it.add("a", 1).add("ab", 2)
    }

    @Test
    fun `matches all prefixes`() = subscriptionTest("abc", setOf(1, 2)) {
        it.add("a", 1).add("ab", 2)
    }

    @Test
    fun `matches multiple same subscriptions only once`() = subscriptionTest("abc", setOf(1)) {
        it.add("a", 1).add("a", 1)
    }

    @Test
    fun `matches multiple subscriptions only once`() = subscriptionTest("abc", setOf(1)) {
        it.add("a", 1).add("ab", 1)
    }

    @Test
    fun `multiple subscriptions are taken in account`() = subscriptionTest("abc", setOf(1)) {
        it.add("a", 1).add("a", 1).remove("a", 1)
    }

    private fun <T> subscriptionTest(
        content: String,
        expectedMatches: Set<T>,
        trieFactory: (SubscriptionTrie<T>) -> SubscriptionTrie<T>,
    ) = subscriptionTest(content.encodeToByteArray(), expectedMatches, trieFactory)

    private fun <T> subscriptionTest(
        content: ByteArray,
        expectedMatches: Set<T>,
        trieFactory: (SubscriptionTrie<T>) -> SubscriptionTrie<T>,
    ) = runTest {
        val subscriptions = trieFactory(SubscriptionTrie<T>())
        val matched = mutableMapOf<T, Int>()
        subscriptions.forEachMatching(constantFrameOf(content)) { matched[it] = (matched[it] ?: 0) + 1 }

        assertEquals(expectedMatches, matched.keys)
        if (matched.values.isNotEmpty()) assertEquals(setOf(1), matched.values.toSet())
    }

    private fun <T> SubscriptionTrie<T>.add(prefix: String, element: T) =
        add(prefix.encodeToByteArray(), element)

    private fun <T> SubscriptionTrie<T>.remove(prefix: String, element: T) =
        remove(prefix.encodeToByteArray(), element)
}
