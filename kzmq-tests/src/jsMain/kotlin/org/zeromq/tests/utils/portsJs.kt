/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import kotlinx.coroutines.*
import kotlin.js.Promise

@JsModule("find-open-port")
@JsNonModule
internal external fun findPort(): Promise<Int>

actual suspend fun findOpenPort(): Int = findPort().await()
