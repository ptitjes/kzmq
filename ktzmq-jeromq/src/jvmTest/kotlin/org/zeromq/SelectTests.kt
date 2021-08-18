package org.zeromq

import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SelectTests {
    //    @Test
    fun onReceiveTest() = runBlocking {
        val context = Context(JeroMQ)
        val data1 = "Data 1".encodeToByteArray()
        val data2 = "Data 2".encodeToByteArray()

        val publisher1 = context.createPublisher()
        publisher1.bind("inproc://pub1")

        val publisher2 = context.createPublisher()
        publisher2.bind("inproc://pub2")

        val subscriber1 = context.createSubscriber()
        subscriber1.connect("inproc://pub1")
        subscriber1.subscribe("")

        val subscriber2 = context.createSubscriber()
        subscriber2.connect("inproc://pub2")
        subscriber2.subscribe("")

        val publication = launch {
            delay(100)
            debugSuspending("pub1") { publisher1.send(Message(data1)) }
            delay(100)
            debugSuspending("pub2") { publisher2.send(Message(data2)) }
        }

        val subscription = launch {
            val received = mutableListOf<ByteArray>()
            repeat(2) {
                received += select<Message> {
                    subscriber1.produceIn(this@runBlocking).onReceive { it }
                    subscriber2.produceIn(this@runBlocking).onReceive { it }
                }.singleOrThrow()

                println(received.map(ByteArray::decodeToString))
            }

            debug("assert") {
                assertContentEquals(
                    listOf(data1, data2).map { it.decodeToString() },
                    received.map { it.decodeToString() }
                )
            }
        }

        subscription.join()
        publication.join()
    }

    @Test
    fun onReceiveTest2() = runBlocking {
        val data1 = "Data 1".encodeToByteArray()
        val data2 = "Data 2".encodeToByteArray()

        val channel1 = produce<Message> { send(Message(data1)) }
        val channel2 = produce<Message> { send(Message(data2)) }

        val subscription = launch {
            val received = mutableListOf<ByteArray>()
            repeat(2) {
                received += select<Message> {
                    channel1.onReceive { it }
                    channel2.onReceive { it }
                }.singleOrThrow()

                println(received.map(ByteArray::decodeToString))
            }

            debug("assert") {
                assertContentEquals(
                    listOf(data1, data2).map { it.decodeToString() },
                    received.map { it.decodeToString() }
                )
            }
        }

        subscription.join()
    }
}

fun <T> debug(marker: String, block: () -> T): T = try {
    println("{$marker} before")
    block()
} finally {
    println("{$marker} after")
}

suspend fun <T> debugSuspending(marker: String, block: suspend () -> T): T = try {
    println("{$marker} before")
    block()
} finally {
    println("{$marker} after")
}


