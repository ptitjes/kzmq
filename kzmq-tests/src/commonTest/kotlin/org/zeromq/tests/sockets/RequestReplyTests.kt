/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.tests.utils.*
import kotlin.test.*

@Suppress("unused")
val RequestReplyTests by testSuite {

    withContexts("bind-connect") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val requestMessage = Message { writeFrame("Hello, 0MQ!".encodeToByteString()) }
        val replyMessage = Message { writeFrame("Hello back!".encodeToByteString()) }

        val request = ctx1.createRequest().apply { bind(address) }
        val reply = ctx2.createReply().apply { connect(address) }

        waitForConnections()

        request.send(requestMessage.copy())
        assertReceivesExactly(listOf(requestMessage), reply)

        reply.send(replyMessage.copy())
        assertReceivesExactly(listOf(replyMessage), request)
    }

    withContexts("connect-bind") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val requestMessage = Message { writeFrame("Hello, 0MQ!".encodeToByteString()) }
        val replyMessage = Message { writeFrame("Hello back!".encodeToByteString()) }

        val request = ctx1.createRequest().apply { bind(address) }
        val reply = ctx2.createReply().apply { connect(address) }

        waitForConnections()

        request.send(requestMessage.copy())
        assertReceivesExactly(listOf(requestMessage), reply)

        reply.send(replyMessage.copy())
        assertReceivesExactly(listOf(replyMessage), request)
    }

    withContexts("round-robin connected reply sockets").config(
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val request = ctx1.createRequest().apply { bind(address) }

        val reply1 = ctx2.createReply().apply { connect(address) }
        waitForConnections(2)
        val reply2 = ctx2.createReply().apply { connect(address) }
        waitForConnections(2)

        val replies = listOf(reply1, reply2)

        val replyJob = launch {
            replies.forEachIndexed { replyIndex, reply ->
                launch {
                    while (true) {
                        val index = reply.receive {
                            assertEquals("some request", readFrame { readString() })
                            readFrame { readByte() }
                        }

                        reply.send {
                            writeFrame("some reply")
                            writeFrame { writeByte(index) }
                            writeFrame { writeByte(replyIndex.toByte()) }
                        }
                    }
                }
            }
        }

        repeat(10) { index ->
            request.send {
                writeFrame("some request")
                writeFrame { writeByte(index.toByte()) }
            }

            assertReceivesExactly(
                listOf(Message {
                    writeFrame("some reply")
                    writeFrame { writeByte(index.toByte()) }
                    writeFrame { writeByte((index % 2).toByte()) }
                }),
                request,
            )
        }

        replyJob.cancelAndJoin()
    }

    withContexts("fair-queuing request sockets").config(
        skip = setOf("jeromq", "zeromq.js"),
    ) { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)

        val reply = ctx2.createReply().apply { bind(address) }

        val request1 = ctx1.createRequest().apply { connect(address) }
        waitForConnections(2)
        val request2 = ctx1.createRequest().apply { connect(address) }
        waitForConnections(2)

        val requests = listOf(request1, request2)

        val replyJob = launch {
            repeat(10) { replyMessageIndex ->
                val (requestIndex, messageIndex) = reply.receive {
                    assertEquals("some request", readFrame { readString() })
                    val requestIndex = readFrame { readByte() }
                    val messageIndex = readFrame { readByte() }
                    requestIndex to messageIndex
                }

                reply.send {
                    writeFrame("some reply")
                    writeFrame { writeByte(requestIndex) }
                    writeFrame { writeByte(messageIndex) }
                    writeFrame { writeByte(replyMessageIndex.toByte()) }
                }
            }
        }

        coroutineScope {
            requests.forEachIndexed { requestIndex, request ->
                launch {
                    repeat(5) { messageIndex ->
                        request.send {
                            writeFrame("some request")
                            writeFrame { writeByte(requestIndex.toByte()) }
                            writeFrame { writeByte(messageIndex.toByte()) }
                        }

                        val expectedMessageIndex = messageIndex * 2 + requestIndex
                        assertReceivesExactly(
                            listOf(Message {
                                writeFrame("some reply")
                                writeFrame { writeByte(requestIndex.toByte()) }
                                writeFrame { writeByte(messageIndex.toByte()) }
                                writeFrame { writeByte(expectedMessageIndex.toByte()) }
                            }),
                            request,
                        )
                    }
                }
            }
        }

        replyJob.cancelAndJoin()
    }
}
