package org.zeromq.tests.sockets

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

class PushPullTests {

    @Test
    fun bindConnectTest() = contextTests(skipEngines = listOf("jeromq")) {
        test { (context) ->
            val address = randomAddress()
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
            val address = randomAddress()
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

    @Test
    fun flowTest() = contextTests(skipEngines = listOf("jeromq")) {
        test { (context) ->
            val address = randomAddress()
            val messageCount = 10
            val sent = generateMessages(messageCount).asFlow()

            val push = context.createPush()
            push.bind(address)

            val pull = context.createPull()
            pull.connect(address)

            launch {
                val received = pull.consumeAsFlow().take(messageCount)
                assertContentEquals(sent.toList(), received.toList())
            }

            launch {
                sent.collectToSocket(push)
            }
        }
    }

    @Test
    fun selectTest() = contextTests(skipEngines = listOf("jeromq")) {
        test { (context) ->
            val address1 = randomAddress()
            val address2 = randomAddress()

            val messageCount = 10
            val sent = generateMessages(messageCount)

            val push1 = context.createPush()
            push1.bind(address1)

            val push2 = context.createPush()
            push2.bind(address2)

            val pull1 = context.createPull()
            pull1.connect(address1)

            val pull2 = context.createPull()
            pull2.connect(address2)

            launch {
                val received = mutableListOf<Message>()
                repeat(messageCount) {
                    received += select<Message> {
                        pull1.onReceive { it }
                        pull2.onReceive { it }
                    }
                }
                assertContentEquals(sent, received.sortedWith(MessageComparator))
            }

            launch {
                for ((index, message) in sent.withIndex()) {
                    (if (index % 2 == 0) push1 else push2).send(message)
                }
            }
        }
    }
}
