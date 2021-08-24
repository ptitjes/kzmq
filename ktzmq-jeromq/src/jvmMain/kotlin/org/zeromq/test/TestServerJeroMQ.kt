package org.zeromq.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.zeromq.Context
import org.zeromq.JeroMQ
import org.zeromq.Message

fun main(): Unit = runBlocking {
    val context = Context(JeroMQ)
    val publisher = context.createPublisher()
    publisher.bind("tcp://localhost:9999")

    launch {
        var value = 0
        while (true) {
            val data = "Data $value"
            publisher.send(Message(data.encodeToByteArray()))
            println("Sent: $data")
            delay(1000)
            value++
        }
    }
}
