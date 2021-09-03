package org.zeromq.tests.transports

import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

class TcpTests {

    @Test
    fun bindConnectTest() = contextTests(skipEngines = listOf("jeromq")) {
        test { (context) ->
            val address = randomAddress(Protocol.TCP)
            val sent = Message("Hello 0MQ!".encodeToByteArray())

            val push = context.createPush()
            push.bind(address)

            val pull = context.createPull()
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
    fun connectBindTest() = contextTests(skipEngines = listOf("jeromq")) {
        test { (context) ->
            val address = randomAddress(Protocol.TCP)
            val sent = Message("Hello 0MQ!".encodeToByteArray())

            val push = context.createPush()
            push.connect(address)

            val pull = context.createPull()
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
