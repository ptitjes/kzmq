package org.zeromq.tests.transports

import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

@Ignore
class TcpTests {

    @Test
    fun bindConnectTest() = contextTests {
        test { (ctx1, ctx2) ->
            val address = randomAddress(Protocol.TCP)
            val sent = Message("Hello 0MQ!".encodeToByteArray())

            val push = ctx1.createPush()
            push.bind(address)

            val pull = ctx2.createPull()
            pull.connect(address)

            launch {
                val received = pull.receive()
                assertEquals(sent, received)
            }

            launch {
                push.send(sent)
            }
        }
    }

    @Test
    fun connectBindTest() = contextTests {
        test { (ctx1, ctx2) ->
            val address = randomAddress(Protocol.TCP)
            val sent = Message("Hello 0MQ!".encodeToByteArray())

            val push = ctx1.createPush()
            push.connect(address)

            val pull = ctx2.createPull()
            pull.bind(address)

            launch {
                val received = pull.receive()
                assertEquals(sent, received)
            }

            launch {
                push.send(sent)
            }
        }
    }
}
