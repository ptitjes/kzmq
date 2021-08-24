package org.zeromq

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val context = Context(CIO)

    val subscriber = context.createSubscriber()
    subscriber.connect("tcp://localhost:9999")
    subscriber.subscribe("")

    launch {
        for (message in subscriber) {
            val data = message.singleOrThrow().decodeToString()
            println("Received: $data")
        }
    }
}
