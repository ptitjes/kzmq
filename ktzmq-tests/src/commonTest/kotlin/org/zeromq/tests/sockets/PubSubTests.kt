package org.zeromq.tests.sockets

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

class PubSubTests {

    private fun pubSubTest(testBlock: suspend CoroutineScope.(sockets: Pair<PublisherSocket, SubscriberSocket>) -> Unit) =
        contextTests {
            test { (ctx1, ctx2) ->
                val pub = ctx1.createPublisher()
                val sub = ctx2.createSubscriber()

                try {
                    val address = randomAddress()

                    pub.bind(address)
                    sub.connect(address)

                    sub.subscribe("")

                    waitForSubscriptions()

                    testBlock(pub to sub)

                } finally {
                    pub.close()
                    sub.close()
                }
            }
        }

    @Test
    fun bindConnectTest() = pubSubTest { (pub, sub) ->
        val sent = Message("Hello 0MQ!".encodeToByteArray())

        launch {
            val received = sub.receive()
            assertEquals(sent, received)
        }

        launch {
            pub.send(sent)
        }
    }

    @Test
    fun connectBindTest() = contextTests {
        test { (ctx1, ctx2) ->
            val address = randomAddress()
            val sent = Message("Hello 0MQ!".encodeToByteArray())

            val publisher = ctx1.createPublisher()
            publisher.connect(address)

            val subscriber = ctx2.createSubscriber()
            subscriber.bind(address)
            subscriber.subscribe("")

            waitForSubscriptions()

            launch {
                val received = subscriber.receive()
                assertEquals(sent, received)
            }

            launch {
                publisher.send(sent)
            }
        }
    }

    @Test
    fun flowTest() = contextTests {
        test { (context) ->
            val address = randomAddress()
            val messageCount = 10
            val sent = generateMessages(messageCount).asFlow()

            val publisher = context.createPublisher()
            publisher.bind(address)

            val subscriber = context.createSubscriber()
            subscriber.connect(address)
            subscriber.subscribe("")

            waitForSubscriptions()

            launch {
                val received = subscriber.consumeAsFlow().take(messageCount)
                assertContentEquals(sent.toList(), received.toList())
            }

            launch {
                sent.collectToSocket(publisher)
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

            val publisher1 = context.createPublisher()
            publisher1.bind(address1)

            val publisher2 = context.createPublisher()
            publisher2.bind(address2)

            val subscriber1 = context.createSubscriber()
            subscriber1.connect(address1)
            subscriber1.subscribe("")

            val subscriber2 = context.createSubscriber()
            subscriber2.connect(address2)
            subscriber2.subscribe("")

            waitForSubscriptions()

            launch {
                val received = mutableListOf<Message>()
                repeat(messageCount) {
                    received += select<Message> {
                        subscriber1.onReceive { it }
                        subscriber2.onReceive { it }
                    }
                }
                assertContentEquals(sent, received.sortedWith(MessageComparator))
            }

            launch {
                for ((index, message) in sent.withIndex()) {
                    (if (index % 2 == 0) publisher1 else publisher2).send(message)
                }
            }
        }
    }

    private suspend fun waitForSubscriptions() = delay(200)
}
