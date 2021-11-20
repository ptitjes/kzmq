package org.zeromq.tests.utils

import org.zeromq.*

actual val engines: List<Engine> = listOf(CIO)

actual val OS_NAME: String
    get() = TODO("Not yet implemented")
