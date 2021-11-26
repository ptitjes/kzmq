/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

val testScope = MainScope()
val testCoroutineContext: CoroutineContext = testScope.coroutineContext
fun runBlockingTest(block: suspend CoroutineScope.() -> Unit): dynamic =
    testScope.promise { this.block() }
