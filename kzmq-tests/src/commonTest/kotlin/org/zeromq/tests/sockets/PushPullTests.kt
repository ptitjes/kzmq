/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.tests.utils.*

private val HELLO = constantFrameOf("Hello 0MQ!")

@Suppress("unused")
class PushPullTests : FunSpec({

    withEngines("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        push.send(messageOf(HELLO))
        pull.receive() shouldBe messageOf(HELLO)
    }

    withEngines("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress()

        val push = ctx1.createPush()
        push.connect(address)

        val pull = ctx2.createPull()
        pull.bind(address)

        push.send(messageOf(HELLO))
        pull.receive() shouldBe messageOf(HELLO)
    }

    withEngines("flow") { (ctx1, ctx2) ->
        val address = randomAddress()
        val count = 10
        val frames = generateRandomFrames(count)

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        coroutineScope {
            launch {
                frames.asFlow().map { messageOf(it) }.collectToSocket(push)
            }

            launch {
                val received = pull.consumeAsFlow().take(count)
                received.toList().map { it.removeFirst() } shouldContainExactly frames
            }
        }
    }

    withEngines("select").config(skipEngines = listOf("jeromq", "zeromq.js")) { (ctx1, ctx2) ->
        val address1 = randomAddress()
        val address2 = randomAddress()

        val count = 10
        val frames = generateRandomFrames(count).sortedWith(FrameComparator)

        val push1 = ctx1.createPush()
        push1.bind(address1)

        val push2 = ctx1.createPush()
        push2.bind(address2)

        val pull1 = ctx2.createPull()
        pull1.connect(address1)

        val pull2 = ctx2.createPull()
        pull2.connect(address2)

        coroutineScope {
            launch {
                for ((index, frame) in frames.withIndex()) {
                    (if (index % 2 == 0) push1 else push2).send(messageOf(frame))
                }
            }

            launch {
                val received = mutableListOf<Frame>()
                repeat(count) {
                    received += select<Message> {
                        pull1.onReceive { it }
                        pull2.onReceive { it }
                    }.removeFirst()
                }
                received.sortedWith(FrameComparator) shouldContainExactly frames
            }
        }
    }
})
