package org.zeromq.wire

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.zeromq.Type
import kotlin.coroutines.CoroutineContext

internal suspend fun startRawSocket(
    socketType: Type,
    socket: Socket,
    isServer: Boolean
): RawSocket = coroutineScope {
    val input = socket.openReadChannel()
    val output = socket.openWriteChannel(autoFlush = true)

    try {
        output.writeGreetingPart1()

        val peerMajorVersion = input.readGreetingPart1()
        if (peerMajorVersion != 3)
            protocolError("Incompatible version $peerMajorVersion.x")

        output.writeGreetingPart2(Mechanism.NULL, false)

        val (_, peerSecuritySpec) = input.readGreetingPart2()
        when (peerSecuritySpec.mechanism) {
            Mechanism.NULL -> nullMechanismHandshake(socketType, isServer, input, output)
            else -> protocolError("Unsupported mechanism ${peerSecuritySpec.mechanism}")
        }

        RawSocket(coroutineContext, input, output)
    } catch (e: Throwable) {
        socket.close()
        throw e
    }
}

internal class RawSocket(
    override val coroutineContext: CoroutineContext,
    input: ByteReadChannel,
    output: ByteWriteChannel
) : CoroutineScope {

    private val _receiveChannel = Channel<CommandOrMessage>()
    private val _sendChannel = Channel<CommandOrMessage>()

    init {
        launch {
            while (true) {
                val commandOrMessage = input.readCommandOrMessage()
                _receiveChannel.send(commandOrMessage)
            }
        }
        launch {
            while (true) {
                val commandOrMessage = _sendChannel.receive()
                output.writeCommandOrMessage(commandOrMessage)
            }
        }
    }

    val receiveChannel: ReceiveChannel<CommandOrMessage> get() = _receiveChannel
    val sendChannel: SendChannel<CommandOrMessage> get() = _sendChannel
}
