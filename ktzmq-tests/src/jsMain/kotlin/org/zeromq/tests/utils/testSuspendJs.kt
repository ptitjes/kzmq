package org.zeromq.tests.utils

import kotlinx.coroutines.*
import kotlin.coroutines.*

val testScope = MainScope()

/**
 * Test runner for js suspend tests.
 */
@OptIn(DelicateCoroutinesApi::class)
actual fun testSuspend(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): dynamic = testScope.promise(block = block, context = context)
