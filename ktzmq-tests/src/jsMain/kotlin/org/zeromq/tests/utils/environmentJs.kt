package org.zeromq.tests.utils

import org.zeromq.Engine
import org.zeromq.JS

// TODO fix JS testSuspend()
actual val engines: List<Engine> = listOf()
    // listOf(JS)

actual val OS_NAME: String
    get() {
        val os = osPlatform()
        return when {
            os.contains("win") -> "win"
            os == "darwin" -> "mac"
            listOf("aix", "freebsd", "linux", "openbsd", "sunos").contains(os) -> "unix"
            else -> "unknown"
        }
    }

@JsModule("os")
@JsNonModule
@JsName("platform")
private external fun osPlatform(): String
