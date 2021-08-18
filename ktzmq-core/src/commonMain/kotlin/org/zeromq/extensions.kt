package org.zeromq

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn

fun ReceiveSocket.consumeAsFlow(): Flow<Message> = flow {
    while (true) emit(receive())
}

suspend fun Flow<Message>.collectToSocket(socket: SendSocket) = collect {
    socket.send(it)
}

@OptIn(FlowPreview::class)
fun ReceiveSocket.produceIn(scope: CoroutineScope): ReceiveChannel<Message> =
    consumeAsFlow().produceIn(scope)
