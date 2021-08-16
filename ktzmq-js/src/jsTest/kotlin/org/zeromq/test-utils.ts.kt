package org.zeromq

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext

val testScope = MainScope()
val testCoroutineContext: CoroutineContext = testScope.coroutineContext
fun runBlockingTest(block: suspend CoroutineScope.() -> Unit): dynamic =
    testScope.promise { this.block() }
