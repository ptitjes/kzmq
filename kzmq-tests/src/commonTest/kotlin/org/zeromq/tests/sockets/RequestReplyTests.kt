/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.core.*
import io.kotest.matchers.equals.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.test.*
import org.zeromq.tests.utils.*

@Suppress("unused")
val RequestReplyTests by testSuite {

    dualContextTest("bind-connect") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val requestTemplate = message {
            writeFrame("Hello, 0MQ!".encodeToByteString())
        }
        val replyTemplate = message {
            writeFrame("Hello back!".encodeToByteString())
        }

        val request = ctx1.createRequest().apply { bind(address) }
        val reply = ctx2.createReply().apply { connect(address) }

        waitForConnections()

        request.send(requestTemplate)
        reply shouldReceive requestTemplate

        reply.send(replyTemplate)
        request shouldReceive replyTemplate
    }

    dualContextTest("connect-bind") { ctx1, ctx2, protocol ->
        val address = randomEndpoint(protocol)
        val requestTemplate = message {
            writeFrame("Hello, 0MQ!".encodeToByteString())
        }
        val replyTemplate = message {
            writeFrame("Hello back!".encodeToByteString())
        }

        val request = ctx1.createRequest().apply { bind(address) }
        val reply = ctx2.createReply().apply { connect(address) }

        waitForConnections()

        request.send(requestTemplate)
        reply shouldReceive requestTemplate

        reply.send(replyTemplate)
        request shouldReceive replyTemplate
    }

    dualContextTest("round-robin connected reply sockets", config = {
        skip("jeromq", "zeromq.js")
    }) { ctx1, ctx2, protocol ->
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
                            readFrame { readString() } shouldBeEqual "some request"
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

            request shouldReceive message {
                writeFrame("some reply")
                writeFrame { writeByte(index.toByte()) }
                writeFrame { writeByte((index % 2).toByte()) }
            }
        }

        replyJob.cancelAndJoin()
    }

    dualContextTest("fair-queuing request sockets", config = {
        skip("jeromq", "zeromq.js", "cio")
    }) { ctx1, ctx2, protocol ->
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
                    readFrame { readString() } shouldBeEqual "some request"
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
                        request shouldReceive message {
                            writeFrame("some reply")
                            writeFrame { writeByte(requestIndex.toByte()) }
                            writeFrame { writeByte(messageIndex.toByte()) }
                            writeFrame { writeByte(expectedMessageIndex.toByte()) }
                        }
                    }
                }
            }
        }

        replyJob.cancelAndJoin()
    }
}
