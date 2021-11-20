package org.zeromq.tests.utils

import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * Test runner for jvm suspend tests.
 */
actual fun testSuspend(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Unit = runBlocking(context, block)
