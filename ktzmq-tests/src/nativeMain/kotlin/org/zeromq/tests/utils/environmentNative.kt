package org.zeromq.tests.utils

import org.zeromq.CIO
import org.zeromq.Engine

actual val engines: List<Engine> = listOf(CIO)

actual val OS_NAME: String
    get() = TODO("Not yet implemented")
