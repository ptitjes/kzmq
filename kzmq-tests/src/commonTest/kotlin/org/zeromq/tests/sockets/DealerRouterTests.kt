/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.sockets

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.tests.utils.*

private const val REQUEST_MARKER = "REQ"
private const val REPLY_MARKER = "REP"

@Suppress("unused")
class DealerRouterTests : FunSpec({

    withContexts("base").config(
        // TODO fix when testing more Dealer and Router logic
        skip = setOf("inproc"),
    ) { ctx1, ctx2, protocol ->
        val dealerCount = 2
        val routerCount = 3
        val addresses = Array(routerCount) { randomAddress(protocol) }

        val routers = addresses.map {
            ctx2.createRouter().apply {
                bind(it)
            }
        }
        val dealers = Array(dealerCount) { index ->
            ctx1.createDealer().apply {
                routingId = index.encodeRoutingId()
                addresses.forEach { connect(it) }
            }
        }

        waitForConnections(dealerCount * routerCount)

        class Trace {
            val receivedReplyIds = atomic(setOf<Int>())
        }

        val trace = Trace()

        coroutineScope {
            launch {
                repeat(dealerCount * routerCount) { requestId ->
                    val dealer = dealers[requestId % dealers.size]
                    val requestData = byteArrayOf(requestId.toByte())

                    dealer.send(
                        Message(
                            REQUEST_MARKER.encodeToByteArray(),
                            requestData
                        )
                    )
                }
            }
            routers.forEach { router ->
                launch {
                    repeat(dealerCount) {
                        val request = router.receive()

                        request.frames.size shouldBe 3
                        val dealerIdFrame = request.frames[0]
                        request.frames[1].decodeToString() shouldBe REQUEST_MARKER
                        val requestIdFrame = request.frames[2]

                        router.send(
                            Message(
                                dealerIdFrame,
                                REPLY_MARKER.encodeToByteArray(),
                                requestIdFrame,
                                dealerIdFrame
                            )
                        )
                    }
                }
            }
            dealers.forEach { dealer ->
                launch {
                    repeat(routerCount) {
                        val reply = dealer.receive()

                        reply.frames.size shouldBe 3
                        reply.frames[0].decodeToString() shouldBe REPLY_MARKER
                        val requestIdFrame = reply.frames[1]
                        val dealerIdFrame = reply.frames[2]

                        val requestId = requestIdFrame[0].toInt()
                        val dealerId = dealerIdFrame.decodeRoutingId()

                        dealerId shouldBe dealer.routingId?.decodeRoutingId()
                        dealerId shouldBe requestId % dealerCount

                        trace.receivedReplyIds.getAndUpdate { it + requestId }
                    }
                }
            }
        }

        trace.receivedReplyIds.value shouldBe (0 until 6).toSet()
    }
})

/*
 * TODO Remove when https://github.com/zeromq/zeromq.js/issues/506 is fixed.
 */
private fun Int.encodeRoutingId(): ByteArray = byteArrayOf(1, (this + 1).toByte())
private fun ByteArray.decodeRoutingId(): Int {
    require(size == 2) //{ "Size should be 2, but is $size" }
    require(this[0] == 1.toByte())
    return this[1].toInt() - 1
}
