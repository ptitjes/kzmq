/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import de.infix.testBalloon.framework.core.*
import de.infix.testBalloon.framework.shared.*
import kotlinx.coroutines.test.*
import kotlinx.io.bytestring.*
import kotlin.test.*

val SubscriptionTrieTest by testSuite {

    subscriptionTest("doesn't match null subscription", "any", setOf<Int>()) { it }

    subscriptionTest("matches empty subscription", "any", setOf(1)) {
        it.add("", 1)
    }

    subscriptionTest("matches only prefixes", "a", setOf(1)) {
        it.add("a", 1).add("ab", 2)
    }

    subscriptionTest("matches all prefixes", "abc", setOf(1, 2)) {
        it.add("a", 1).add("ab", 2)
    }

    subscriptionTest("matches multiple same subscriptions only once", "abc", setOf(1)) {
        it.add("a", 1).add("a", 1)
    }

    subscriptionTest("matches multiple subscriptions only once", "abc", setOf(1)) {
        it.add("a", 1).add("ab", 1)
    }

    subscriptionTest("multiple subscriptions are taken in account", "abc", setOf(1)) {
        it.add("a", 1).add("a", 1).remove("a", 1)
    }
}

@TestRegistering
private fun <T> TestSuite.subscriptionTest(
    name: String,
    content: String,
    expectedMatches: Set<T>,
    trieFactory: (SubscriptionTrie<T>) -> SubscriptionTrie<T>,
) = test(name) {
    subscriptionTest(content.encodeToByteArray(), expectedMatches, trieFactory)
}

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

private fun <T> SubscriptionTrie<T>.add(prefix: String, element: T) =
    add(prefix.encodeToByteString(), element)

private fun <T> SubscriptionTrie<T>.remove(prefix: String, element: T) =
    remove(prefix.encodeToByteString(), element)
