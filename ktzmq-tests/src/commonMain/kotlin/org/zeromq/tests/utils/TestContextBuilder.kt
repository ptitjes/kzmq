package org.zeromq.tests.utils

import kotlinx.coroutines.CoroutineScope
import org.zeromq.Context

class TestContextBuilder(
    var test: suspend CoroutineScope.(contexts: Pair<Context, Context>) -> Unit = {},
    var after: suspend (contexts: Pair<Context, Context>) -> Unit = {},
    var timeoutSeconds: Long = 5,
)

fun TestContextBuilder.test(block: suspend CoroutineScope.(contexts: Pair<Context, Context>) -> Unit) {
    test = block
}

fun TestContextBuilder.after(block: suspend (contexts: Pair<Context, Context>) -> Unit) {
    after = block
}
