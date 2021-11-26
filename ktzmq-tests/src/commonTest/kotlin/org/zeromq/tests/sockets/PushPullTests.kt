package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.tests.utils.*

class PushPullTests : FunSpec({

    withEngines("bind-connect").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        push.send(message)
        pull.receive() shouldBe message
    }

    withEngines("connect-bind").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress()
        val message = Message("Hello 0MQ!".encodeToByteArray())

        val push = ctx1.createPush()
        push.connect(address)

        val pull = ctx2.createPull()
        pull.bind(address)

        push.send(message)
        pull.receive() shouldBe message
    }

    withEngines("flow").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
        val address = randomAddress()
        val messageCount = 10
        val sent = generateMessages(messageCount).asFlow()

        val push = ctx1.createPush()
        push.bind(address)

        val pull = ctx2.createPull()
        pull.connect(address)

        coroutineScope {
            launch {
                sent.collectToSocket(push)
            }

            launch {
                val received = pull.consumeAsFlow().take(messageCount)
                received.toList() shouldContainExactly sent.toList()
            }
        }
    }

    withEngines("select").config(skipEngines = listOf("jeromq")) { (ctx1, ctx2) ->
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

        coroutineScope {
            launch {
                for ((index, message) in sent.withIndex()) {
                    (if (index % 2 == 0) push1 else push2).send(message)
                }
            }

            launch {
                val received = mutableListOf<Message>()
                repeat(messageCount) {
                    received += select<Message> {
                        pull1.onReceive { it }
                        pull2.onReceive { it }
                    }
                }
                received.sortedWith(MessageComparator) shouldContainExactly sent
            }
        }
    }
})
