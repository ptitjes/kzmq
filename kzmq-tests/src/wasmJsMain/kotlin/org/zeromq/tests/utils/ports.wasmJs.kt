/*
 * Copyright (c) 2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import kotlinx.coroutines.await
import kotlin.js.Promise

@JsModule("find-open-port")
internal external fun findPort(): Promise<JsNumber>

actual suspend fun findOpenPort(): Int = findPort().await<JsNumber>().toInt()
