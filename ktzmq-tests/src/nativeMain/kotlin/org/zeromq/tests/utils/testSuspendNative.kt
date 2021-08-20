package org.zeromq.tests.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

actual fun testSuspend(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    TODO()
}
