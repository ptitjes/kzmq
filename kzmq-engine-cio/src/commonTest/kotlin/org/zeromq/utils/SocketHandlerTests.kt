/*
 * Copyright (c) 2024-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*

internal suspend fun <H : SocketHandler> (() -> H).runTest(
    test: suspend (
        peerEvents: SendChannel<PeerEvent>,
        send: suspend (Message) -> Unit,
        receive: suspend () -> Message
    ) -> Unit,
) = coroutineScope {
    val handler: H = this@runTest()
    val peerEvents = Channel<PeerEvent>()
    val handlerJob = launch { handler.handle(peerEvents) }
    test(peerEvents, handler::send, handler::receive)
    handlerJob.cancelAndJoin()
}

internal interface TestSetScope {
    fun test(name: String, test: suspend TestScope.() -> Unit)
}

internal fun FunSpec.testSet(setName: String, tests: TestSetScope.() -> Unit) {
    object : TestSetScope {
        override fun test(name: String, test: suspend TestScope.() -> Unit) {
            this@testSet.test("$setName > $name", test)
        }
    }.tests()
}
