package org.zeromq

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

fun Socket.asFlow(): Flow<ByteArray> = flow {
    while (true) emit(receive())
}

suspend fun Flow<ByteArray>.collectToSocket(socket: Socket) = collect {
    socket.send(it)
}
