package org.zeromq.tests.sockets

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

@Ignore
class PushPullTests {

    @Test
    fun bindConnectTest() = contextTests {
        test { (ctx1, ctx2) ->
            val address = randomAddress()
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
            val address = randomAddress()
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

    @Test
    fun flowTest() = contextTests {
        test { (ctx1, ctx2) ->
            val address = randomAddress()
            val messageCount = 10
            val sent = generateMessages(messageCount).asFlow()

            val push = ctx1.createPush()
            push.bind(address)

            val pull = ctx2.createPull()
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
        test { (ctx1, ctx2) ->
            val address1 = randomAddress()
            val address2 = randomAddress()

            val messageCount = 10
            val sent = generateMessages(messageCount)

            val push1 = ctx1.createPush()
            push1.bind(address1)

            val push2 = ctx1.createPush()
            push2.bind(address2)

            val pull1 = ctx2.createPull()
            pull1.connect(address1)

            val pull2 = ctx2.createPull()
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
