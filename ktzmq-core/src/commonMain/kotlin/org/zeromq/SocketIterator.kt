package org.zeromq

interface SocketIterator {
    suspend operator fun hasNext(): Boolean
    operator fun next(): Message
}
