package org.zeromq

interface EngineInstance: SocketFactory {
    fun close()
}
