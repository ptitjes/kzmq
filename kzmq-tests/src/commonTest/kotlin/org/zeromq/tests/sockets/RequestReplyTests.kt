/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import org.zeromq.*
import org.zeromq.tests.utils.*

@Suppress("unused")
class RequestReplyTests : FunSpec({

    withEngines("bind-connect").config(skipEngines = listOf("cio")) { (ctx1, ctx2) ->
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

    withEngines("connect-bind").config(skipEngines = listOf("cio")) { (ctx1, ctx2) ->
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
})
