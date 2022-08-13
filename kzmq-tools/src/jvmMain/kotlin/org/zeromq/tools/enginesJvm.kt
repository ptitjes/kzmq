/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tools

import org.zeromq.*

actual val engines: List<EngineFactory> = listOf(CIO, JeroMQ)
