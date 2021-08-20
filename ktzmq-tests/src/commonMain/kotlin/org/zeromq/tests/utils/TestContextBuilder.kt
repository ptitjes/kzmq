package org.zeromq.tests.utils

import kotlinx.coroutines.CoroutineScope
import org.zeromq.Context

class TestContextBuilder(
    var test: suspend CoroutineScope.(context: Context) -> Unit = {},
    var after: suspend (context: Context) -> Unit = {},
)

fun TestContextBuilder.test(block: suspend CoroutineScope.(context: Context) -> Unit) {
    test = block
}

fun TestContextBuilder.after(block: suspend (context: Context) -> Unit) {
    after = block
}
