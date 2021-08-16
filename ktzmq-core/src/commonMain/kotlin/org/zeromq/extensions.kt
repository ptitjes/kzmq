package org.zeromq

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

fun ReceiveSocket.asFlow(): Flow<Message> = flow {
    while (true) emit(receive())
}

suspend fun Flow<Message>.collectToSocket(socket: SendSocket) = collect {
    socket.send(it)
}
