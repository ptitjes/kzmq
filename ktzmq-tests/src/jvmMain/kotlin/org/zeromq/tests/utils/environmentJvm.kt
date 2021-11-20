package org.zeromq.tests.utils

import org.zeromq.*
import java.util.*

actual val engines: List<Engine> = listOf(CIO, JeroMQ)

actual val OS_NAME: String
    get() {
        val os = System.getProperty("os.name", "unknown").lowercase(Locale.getDefault())
        return when {
            os.contains("win") -> "win"
            os.contains("mac") -> "mac"
            os.contains("nux") -> "unix"
            else -> "unknown"
        }
    }
