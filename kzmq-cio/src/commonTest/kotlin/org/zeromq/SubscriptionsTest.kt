/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.test.*
import kotlin.test.*

internal class SubscriptionsTest {

    @Test
    fun `doesn't match null subscription`() = subscriptionTest("any", setOf<String>()) { it }

    @Test
    fun `matches empty subscription`() = subscriptionTest("any", setOf("test")) {
        it.add("", "test")
    }

    @Test
    fun `matches only prefixes`() = subscriptionTest("a", setOf("test1")) {
        it.add("a", "test1").add("ab", "test2")
    }

    @Test
    fun `matches all prefixes`() = subscriptionTest("abc", setOf("test1", "test2")) {
        it.add("a", "test1").add("ab", "test2")
    }

    @Test
    fun `matches multiple same subscriptions only once`() = subscriptionTest("abc", setOf("test1")) {
        it.add("a", "test1").add("a", "test1")
    }

    @Test
    fun `matches multiple subscriptions only once`() = subscriptionTest("abc", setOf("test1")) {
        it.add("a", "test1").add("ab", "test1")
    }

    @Test
    fun `multiple subscriptions are taken in account`() = subscriptionTest("abc", setOf("test1")) {
        it.add("a", "test1").add("a", "test1").remove("a", "test1")
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
        subscriptions.forEachMatching(content) { matched[it] = (matched[it] ?: 0) + 1 }

        assertEquals(expectedMatches, matched.keys)
        if (matched.values.isNotEmpty()) assertEquals(setOf(1), matched.values.toSet())
    }

    private fun <T> SubscriptionTrie<T>.add(content: String, element: T) =
        add(content.encodeToByteArray(), element)

    private fun <T> SubscriptionTrie<T>.remove(content: String, element: T) =
        remove(content.encodeToByteArray(), element)
}
