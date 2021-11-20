package org.zeromq.tests.utils

import kotlinx.coroutines.*
import kotlin.coroutines.*

actual fun testSuspend(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Unit = runBlocking(context, block)
