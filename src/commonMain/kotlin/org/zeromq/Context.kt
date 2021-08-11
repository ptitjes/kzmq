package org.zeromq

interface Context {
    fun createSocket(type: Type): Socket
}
