package org.zeromq.tests.utils

import java.net.*

actual fun findOpenPort(): Int =
    ServerSocket(0, 0).use { tmpSocket -> return tmpSocket.localPort }
