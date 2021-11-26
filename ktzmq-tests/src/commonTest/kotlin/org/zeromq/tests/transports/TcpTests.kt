package org.zeromq.tests.transports

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import org.zeromq.*
import org.zeromq.tests.utils.*

class TcpTests : FunSpec({

    withEngines("bind-connect").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.TCP)
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        push.send(message)
        pull.receive() shouldBe message
    }

    withEngines("connect-bind").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress(Protocol.TCP)
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.connect(address)

        val pull = ctx2.createPull()
        pull.bind(address)

        push.send(message)
        pull.receive() shouldBe message
    }
})
