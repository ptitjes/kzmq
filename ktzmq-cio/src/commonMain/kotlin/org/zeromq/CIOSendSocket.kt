package org.zeromq

import kotlinx.coroutines.channels.SendChannel

internal interface CIOSendSocket : SendSocket {

    val sendChannel: SendChannel<Message>

    override suspend fun send(message: Message) = sendChannel.send(message)

    override suspend fun sendCatching(message: Message): SocketResult<Unit> {
        TODO("Not yet implemented")
    }

    override fun trySend(message: Message): SocketResult<Unit> {
        val result = sendChannel.trySend(message)
        return if (result.isSuccess) SocketResult.success(Unit)
        else SocketResult.failure(result.exceptionOrNull())
    }

    override var multicastHops: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var sendBufferSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var sendHighWaterMark: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var sendTimeout: Int
        get() = TODO("Not yet implemented")
        set(value) {}
}
