/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import co.touchlab.kermit.*

internal val logger = Logger(
    StaticConfig(
        minSeverity = Severity.Warn,
        logWriterList = listOf(CommonWriter())
    )
)
