/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
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

    withContexts("base") { (ctx1, ctx2) ->

        val dealerCount = 2
        val routerCount = 3
        val addresses = Array(routerCount) { randomAddress() }

        val dealers = Array(dealerCount) { index ->
            ctx1.createDealer().apply {
                routingId = index.encodeRoutingId()
                addresses.forEach { connect(it) }
            }
        }
        val routers = addresses.map {
            ctx2.createRouter().apply {
                bind(it)
            }
        }

        delay(200)

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

                        request.parts.size shouldBe 3
                        val dealerId = request.parts[0]
                        request.parts[1].decodeToString() shouldBe REQUEST_MARKER
                        val requestId = request.parts[2]

                        router.send(
                            Message(
                                dealerId,
                                REPLY_MARKER.encodeToByteArray(),
                                requestId,
                                dealerId
                            )
                        )
                    }
                }
            }
            dealers.forEach { dealer ->
                launch {
                    repeat(routerCount) {
                        val reply = dealer.receive()

                        reply.parts.size shouldBe 3
                        reply.parts[0].decodeToString() shouldBe REPLY_MARKER
                        val requestIdFrame = reply.parts[1]
                        val dealerIdFrame = reply.parts[2]

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
    require(size == 2)
    require(this[0] == 1.toByte())
    return this[1].toInt() - 1
}
