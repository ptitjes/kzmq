/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tools

import kotlinx.coroutines.*
import platform.linux.*
import kotlin.math.*

actual val dispatcher: CoroutineDispatcher = Dispatchers.IO
