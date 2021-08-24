package org.zeromq.wire

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.zeromq.Message
import org.zeromq.Type
import kotlin.coroutines.CoroutineContext

internal suspend fun startRawSocket(
    coroutineContext: CoroutineContext,
    socket: Socket,
    socketType: Type,
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

        val (peerMinorVersion, peerSecuritySpec) = input.readGreetingPart2()
        when (peerSecuritySpec.mechanism) {
            Mechanism.NULL -> nullMechanismHandshake(socketType, isServer, input, output)
            else -> protocolError("Unsupported mechanism ${peerSecuritySpec.mechanism}")
        }

        RawSocket(coroutineContext, socket, input, output, socketType, peerMinorVersion)
    } catch (e: Throwable) {
        socket.close()
        throw e
    }
}

internal class RawSocket(
    override val coroutineContext: CoroutineContext,
    socket: Socket,
    input: ByteReadChannel,
    output: ByteWriteChannel,
    private val socketType: Type,
    private val peerMinorVersion: Int
) : CoroutineScope {

    private val _receiveChannel = Channel<CommandOrMessage>()
    private val _sendChannel = Channel<CommandOrMessage>()

    init {
        launch {
            while (true) {
                val commandOrMessage = input.readCommandOrMessage()

                // Specially handle ZMTP 3.0 subscriptions
                val transformed = if (peerMinorVersion == 0 && socketType == Type.PUB) {
                    transformSubscriptionMessages(commandOrMessage)
                } else commandOrMessage

                _receiveChannel.send(transformed)
            }
        }
        launch {
            while (true) {
                val commandOrMessage = _sendChannel.receive()

                // Specially handle ZMTP 3.0 subscriptions
                val transformed = if (peerMinorVersion == 0 && socketType == Type.SUB) {
                    transformSubscriptionCommands(commandOrMessage)
                } else commandOrMessage

                output.writeCommandOrMessage(transformed)
            }
        }
    }

    val receiveChannel: ReceiveChannel<CommandOrMessage> get() = _receiveChannel
    val sendChannel: SendChannel<CommandOrMessage> get() = _sendChannel
}

private fun transformSubscriptionMessages(commandOrMessage: CommandOrMessage): CommandOrMessage =
    if (commandOrMessage.isMessage) {
        extractSubscriptionCommand(commandOrMessage.messageOrThrow()) ?: commandOrMessage
    } else commandOrMessage

private fun extractSubscriptionCommand(message: Message): CommandOrMessage? {
    if (message.isSingle) {
        val bytes = message.singleOrThrow()
        val firstByte = bytes[0].toInt()
        if (firstByte == 0 || firstByte == 1) {
            val subscribe = firstByte == 1
            val topic = bytes.sliceArray(1 until bytes.size)
            return CommandOrMessage(
                if (subscribe) SubscribeCommand(topic) else CancelCommand(topic)
            )
        }
    }
    return null
}

private fun transformSubscriptionCommands(commandOrMessage: CommandOrMessage): CommandOrMessage =
    if (commandOrMessage.isCommand) {
        when (val command = commandOrMessage.commandOrThrow()) {
            is SubscribeCommand ->
                CommandOrMessage(buildSubscriptionMessage(true, command.topic))
            is CancelCommand ->
                CommandOrMessage(buildSubscriptionMessage(false, command.topic))
            else -> commandOrMessage
        }
    } else commandOrMessage

private fun buildSubscriptionMessage(subscribe: Boolean, topic: ByteArray): Message {
    val bytes = ByteArray(topic.size + 1) { index ->
        if (index == 0) if (subscribe) 1 else 0
        else topic[index - 1]
    }
    return Message(bytes)
}
