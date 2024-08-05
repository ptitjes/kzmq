/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import kotlinx.coroutines.channels.*
import kotlinx.io.bytestring.*
import org.zeromq.internal.*

internal class SubscriptionManager {
    val existing = mutableListOf<ByteString>()
    val lateSubscriptionCommands = Channel<Command>(10)

    suspend fun subscribe(topics: List<ByteString>) {
        val effectiveTopics = topics.ifEmpty { listOf(ByteString()) }

        existing.addAll(effectiveTopics)

        for (topic in effectiveTopics) {
            lateSubscriptionCommands.send(SubscribeCommand(topic))
        }
    }

    suspend fun unsubscribe(topics: List<ByteString>) {
        val effectiveTopics = topics.ifEmpty { listOf(ByteString()) }

        val removedTopics = mutableListOf<ByteString>()
        for (topic in effectiveTopics) {
            if (existing.remove(topic)) removedTopics += topic
        }

        for (topic in removedTopics) {
            lateSubscriptionCommands.send(CancelCommand(topic))
        }
    }
}
