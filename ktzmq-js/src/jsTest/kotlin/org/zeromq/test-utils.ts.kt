package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

val testScope = MainScope()
val testCoroutineContext: CoroutineContext = testScope.coroutineContext
fun runBlockingTest(block: suspend CoroutineScope.() -> Unit): dynamic =
    testScope.promise { this.block() }
