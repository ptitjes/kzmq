/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class XPublisherSubscriberTests : FunSpec({

    withContexts("subscription filter") { (ctx1, ctx2) ->
        val address = randomAddress()

        val sent = listOf("prefixed data", "non-prefixed data", "prefix is good")
        val expected = sent.filter { it.startsWith("prefix") }

        val publisher = ctx1.createXPublisher()
        publisher.bind(address)

        val subscriber = ctx2.createSubscriber()
        subscriber.connect(address)

        waitForSubscriptions()

        coroutineScope {
            launch {
                subscriber.subscribe("prefix")
            }

            launch {
                val message = publisher.receive()
                val subscribeTopicPair = destructureSubscriptionMessage(message)
                subscribeTopicPair shouldNotBe null
                subscribeTopicPair?.let { (subscribe, topic) ->
                    subscribe shouldBe true
                    topic.decodeToString() shouldBe "prefix"
                }
            }
        }

        waitForSubscriptions()

        coroutineScope {
            launch {
                sent.forEach { publisher.send(Message(it.encodeToByteArray())) }
            }

            launch {
                val received = mutableListOf<String>()
                repeat(2) {
                    received += subscriber.receive().singleOrThrow().decodeToString()
                }
                received shouldContainExactly expected
            }
        }
    }
})
