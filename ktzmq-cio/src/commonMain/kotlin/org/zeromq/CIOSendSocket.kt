package org.zeromq

import kotlinx.coroutines.channels.*
import org.zeromq.internal.*

internal interface CIOSendSocket : SendSocket {

    val sendChannel: SendChannel<Message>
    val socketOptions: SocketOptions

    override suspend fun send(message: Message) = sendChannel.send(message)

    override suspend fun sendCatching(message: Message): SocketResult<Unit> = try {
        sendChannel.send(message)
        SocketResult.success(Unit)
    } catch (t: Throwable) {
        SocketResult.failure(t)
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
        get() = socketOptions.sendQueueSize
        set(value) {
            socketOptions.sendQueueSize = value
        }

    override var sendTimeout: Int
        get() = TODO("Not yet implemented")
        set(value) {}
}
