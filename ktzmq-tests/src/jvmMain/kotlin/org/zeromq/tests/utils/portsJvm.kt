package org.zeromq.tests.utils

import java.net.ServerSocket

actual fun findOpenPort(): Int =
    ServerSocket(0, 0).use { tmpSocket -> return tmpSocket.localPort }
