/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.tests.utils.*

private val HELLO_REQUEST = constantFrameOf("Hello 0MQ!")
private val HELLO_REPLY = constantFrameOf("Hello back!")

@Suppress("unused")
class RequestReplyTests : FunSpec({

    withEngines("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()

        val request = ctx1.createRequest()
        request.bind(address)

        val reply = ctx2.createReply()
        reply.connect(address)

        request.send(messageOf(HELLO_REQUEST))
        reply.receive() shouldBe messageOf(HELLO_REQUEST)

        reply.send(messageOf(HELLO_REPLY))
        request.receive() shouldBe messageOf(HELLO_REPLY)
    }

    withEngines("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress()

        val request = ctx1.createRequest()
        request.bind(address)

        val reply = ctx2.createReply()
        reply.connect(address)

        request.send(messageOf(HELLO_REQUEST))
        reply.receive() shouldBe messageOf(HELLO_REQUEST)

        reply.send(messageOf(HELLO_REPLY))
        request.receive() shouldBe messageOf(HELLO_REPLY)
    }

    withEngines("round-robin connected reply sockets").config(
        skipEngines = listOf("jeromq", "zeromq.js")
    ) { (ctx1, ctx2) ->
        val address = randomAddress()

        val request = ctx1.createRequest()
        request.bind(address)

        val reply1 = ctx2.createReply()
        reply1.connect(address)

        val reply2 = ctx2.createReply()
        reply2.connect(address)

        // Wait for all connections to happen
        delay(100)

        var lastReplier: ReplySocket? = null
        repeat(10) { i ->
            val indexFrame = constantFrameOf(ByteArray(1) { i.toByte() })
            val requestMessage = messageOf(HELLO_REQUEST, indexFrame)
            val replyMessage = messageOf(HELLO_REPLY, indexFrame)

            request.send(requestMessage)

            select<Unit> {
                reply1.onReceive { message ->
                    lastReplier shouldNotBe reply1
                    lastReplier = reply1

                    message.removeFirst() shouldBe HELLO_REQUEST
                    message.removeFirst() shouldBe indexFrame

                    reply1.send(replyMessage)
                }
                reply2.onReceive { message ->
                    lastReplier shouldNotBe reply2
                    lastReplier = reply2

                    message.removeFirst() shouldBe HELLO_REQUEST
                    message.removeFirst() shouldBe indexFrame

                    reply2.send(replyMessage)
                }
            }

            val reply = request.receive()
            reply.removeFirst() shouldBe HELLO_REPLY
            reply.removeFirst() shouldBe indexFrame
        }
    }

    withEngines("fair-queuing request sockets").config(
        skipEngines = listOf("cio", "jeromq", "zeromq.js")
    ) { (ctx1, ctx2) ->
        val address = randomAddress()

        val request1 = ctx1.createRequest()
        request1.connect(address)

        val request2 = ctx1.createRequest()
        request2.connect(address)

        val reply = ctx2.createReply()
        reply.bind(address)

        coroutineScope {
            launch {
                var lastRequester: String? = null
                repeat(10) {
                    val request = reply.receive()

                    request.removeFirst() shouldBe HELLO_REQUEST
                    val number = request.removeFirst()

                    reply.send(messageOf(HELLO_REPLY, number))
                }
            }
            launch {
                val numberFrame = constantFrameOf(ByteArray(1) { 1 })
                val requestMessage = messageOf(HELLO_REQUEST, numberFrame)
                val replyMessage = messageOf(HELLO_REPLY, numberFrame)

                repeat(5) {
                    request1.send(requestMessage)
                    request1.receive() shouldBe replyMessage
                }
            }
            launch {
                val numberFrame = constantFrameOf(ByteArray(1) { 2 })
                val requestMessage = messageOf(HELLO_REQUEST, numberFrame)
                val replyMessage = messageOf(HELLO_REPLY, numberFrame)

                repeat(5) {
                    request2.send(requestMessage)
                    request2.receive() shouldBe replyMessage
                }
            }
        }
    }
})
