package org.zeromq.tests.utils

import kotlinx.coroutines.*
import kotlin.coroutines.*

expect fun testSuspend(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
)
