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

@Suppress("unused")
class RequestReplyTests : FunSpec({

    withContexts("bind-connect") { (ctx1, ctx2) ->
        val address = randomAddress()
        val requestMessage = Message("Hello 0MQ!".encodeToByteArray())
        val replyMessage = Message("Hello back!".encodeToByteArray())

        val request = ctx1.createRequest()
        request.bind(address)

        val reply = ctx2.createReply()
        reply.connect(address)

        request.send(requestMessage)
        reply.receive() shouldBe requestMessage

        reply.send(replyMessage)
        request.receive() shouldBe replyMessage
    }

    withContexts("connect-bind") { (ctx1, ctx2) ->
        val address = randomAddress()
        val requestMessage = Message("Hello 0MQ!".encodeToByteArray())
        val replyMessage = Message("Hello back!".encodeToByteArray())

        val request = ctx1.createRequest()
        request.bind(address)

        val reply = ctx2.createReply()
        reply.connect(address)

        request.send(requestMessage)
        reply.receive() shouldBe requestMessage

        reply.send(replyMessage)
        request.receive() shouldBe replyMessage
    }

    withContexts("round-robin connected reply sockets").config(
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
            val requestMessage = Message("Hello $i".encodeToByteArray())
            val replyMessage = Message("Hello back $i".encodeToByteArray())

            request.send(requestMessage)

            select<Unit> {
                reply1.onReceive { message ->
                    lastReplier shouldNotBe reply1
                    lastReplier = reply1

                    message shouldBe requestMessage
                    reply1.send(replyMessage)
                }
                reply2.onReceive { message ->
                    lastReplier shouldNotBe reply2
                    lastReplier = reply2

                    message shouldBe requestMessage
                    reply2.send(replyMessage)
                }
            }

            request.receive() shouldBe replyMessage
        }
    }

    withContexts("fair-queuing request sockets").config(
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
                    val requester = request.singleOrThrow().decodeToString().substringAfterLast(" ")

                    lastRequester shouldNotBe requester
                    lastRequester = requester

                    reply.send(Message("Hello back to $requester".encodeToByteArray()))
                }
            }
            launch {
                val requestMessage = Message("Hello from request1".encodeToByteArray())
                val replyMessage = Message("Hello back to request1".encodeToByteArray())

                repeat(5) {
                    request1.send(requestMessage)
                    request1.receive() shouldBe replyMessage
                }
            }
            launch {
                val requestMessage = Message("Hello from request2".encodeToByteArray())
                val replyMessage = Message("Hello back to request2".encodeToByteArray())

                repeat(5) {
                    request2.send(requestMessage)
                    request2.receive() shouldBe replyMessage
                }
            }
        }
    }
})
