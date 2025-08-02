/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import de.infix.testBalloon.framework.*
import io.kotest.matchers.*
import io.kotest.matchers.equals.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.tests.utils.*

private const val REQUEST_MARKER = "REQ"
private const val REPLY_MARKER = "REP"

@Suppress("unused")
val DealerRouterTests by testSuite {

    withContexts("base").config(
        // TODO fix when testing more Dealer and Router logic
//        skip = setOf("jeromq"),
    ) { ctx1, ctx2, protocol ->
        val dealerCount = 2
        val routerCount = 3
        val addresses = Array(routerCount) { randomEndpoint(protocol) }

        val routers = addresses.map {
            ctx2.createRouter().apply { bind(it) }
        }
        val dealers = Array(dealerCount) { index ->
            ctx1.createDealer().apply {
                routingId = index.encodeAsRoutingId()
                addresses.forEach { connect(it) }
                waitForConnections(addresses.size)
            }
        }

        class Trace {
            val receivedReplyIds = atomic(setOf<Int>())
        }

        val trace = Trace()

        coroutineScope {
            launch {
                repeat(dealerCount * routerCount) { requestId ->
                    val dealerId = requestId % dealers.size
                    val dealer = dealers[dealerId]

                    dealer.send {
                        writeFrame(REQUEST_MARKER)
                        writeFrame { writeByte(requestId.toByte()) }
                    }
                }
            }
            routers.forEach { router ->
                launch {
                    repeat(dealerCount) {
                        val (dealerId, requestId) = router.receive {
                            val dealerId = readFrame { readByteString() }
                            readFrame { readString() shouldBe REQUEST_MARKER }
                            val requestId = readFrame { readByte() }
                            dealerId to requestId
                        }

                        router.send {
                            writeFrame(dealerId)
                            writeFrame(REPLY_MARKER)
                            writeFrame { writeByte(requestId) }
                            writeFrame(dealerId)
                        }
                    }
                }
            }
            dealers.forEach { dealer ->
                launch {
                    repeat(routerCount) {
                        val (dealerId, requestId) = dealer.receive {
                            readFrame { readString() shouldBe REPLY_MARKER }
                            val requestId = readFrame { readByte() }
                            val dealerId = readFrame { readByteString() }
                            dealerId to requestId
                        }

                        val realDealerId = dealerId.decodeFromRoutingId()
                        realDealerId shouldBe dealer.routingId?.decodeFromRoutingId()
                        realDealerId shouldBe requestId % dealerCount

                        trace.receivedReplyIds.getAndUpdate { it + requestId.toInt() }
                    }
                }
            }
        }

        trace.receivedReplyIds.value shouldBeEqual (0 until 6).toSet()
    }
}

/*
 * TODO Remove when https://github.com/zeromq/zeromq.js/issues/506 is fixed.
 */
private fun Int.encodeAsRoutingId(): ByteString = buildByteString {
    append(1.toByte())
    append((this@encodeAsRoutingId + 1).toByte())
}

private fun ByteString.decodeFromRoutingId(): Int {
    require(size == 2) { "Size should be 2, but is $size" }
    require(this[0] == 1.toByte())
    return this[1].toInt() - 1
}
