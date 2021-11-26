/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package temp

import kotlinx.coroutines.*
import org.zeromq.*

fun main(): Unit = runBlocking {
    val context = Context(JeroMQ)

    launch {
        val publisher = context.createPublisher()
        publisher.connect("tcp://localhost:9000")

        var i = 0
        while (true) {
            val msg = Message("$i".encodeToByteArray())
            publisher.send(msg)
            i++
        }
    }

    launch {
        val subscriber = context.createSubscriber()
        subscriber.bind("tcp://localhost:9000")
        subscriber.subscribe("")

        while (true) {
            val msg = subscriber.receive()
            println(msg.singleOrThrow().decodeToString())
        }
    }
}
