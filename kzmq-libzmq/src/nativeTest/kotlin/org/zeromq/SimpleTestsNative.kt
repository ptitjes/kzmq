/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

class SimpleTestsNative {
//    @Test
//    fun test() = runBlocking {
//        println("Hello native")
//        val context = LibzmqContext()
//        val message = "Hello 0MQ!".encodeToByteArray()
//
//        val publication = launch {
//            val publisher = context.createSocket(Type.PUB)
//            publisher.bind("inproc://testSimple")
//
//            println("Before delay")
//            delay(100)
//            println("Sending message")
//            publisher.send(message)
//            println("Sent message")
//        }
//
//        val subscription = launch {
//            val subscriber = context.createSocket(Type.SUB)
//            subscriber.connect("inproc://testSimple")
//            subscriber.subscribe("")
//
//            println("Receiving message")
//            var received: ByteArray?
//            while (true) {
//                received = subscriber.receiveOrNull()
//                if (received != null) break
//            }
//            //            assertEquals(sent, received)
//            println("Received message: ${received?.toKString()}")
//        }
//
//        subscription.join()
//        publication.join()
//    }

//    @Test
//    fun testSimple() = runBlocking {
//        val context = LibzmqContext()
//        val sent = "Hello 0MQ!"
//
//        val publication = launch {
//            val publisher = context.createSocket(Type.PUB)
//            publisher.bind("inproc://testSimple")
//
//            delay(100)
//            publisher.send(sent.encodeToByteArray())
//        }
//
//        val subscription = launch {
//            val subscriber = context.createSocket(Type.SUB)
//            subscriber.connect("inproc://testSimple")
//            subscriber.subscribe("")
//
//            val received = subscriber.receive().toKString()
//            assertEquals(sent, received)
//        }
//
//        subscription.join()
//        publication.join()
//    }
//
//    @Test
//    fun testFlow() = runBlocking {
//        val context = LibzmqContext()
//        val intFlow: Flow<Int> = flow { for (i in 0..9) emit(i) }
//
//        val publication = launch {
//            val publisher = context.createSocket(Type.PUB)
//            publisher.bind("inproc://testFlow")
//
//            delay(100)
//            intFlow
//                .map { it.toString().encodeToByteArray() }
//                .collectToSocket(publisher)
//        }
//
//        val subscription = launch {
//            val subscriber = context.createSocket(Type.SUB)
//            subscriber.connect("inproc://testFlow")
//            subscriber.subscribe("")
//
//            val values = subscriber.asFlow().take(10).map { it.toKString().toInt() }
//            assertContentEquals(intFlow.toList(), values.toList())
//        }
//
//        subscription.join()
//        publication.join()
//    }
}
