/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.network.sockets.*
import io.ktor.network.sockets.Socket
import kotlinx.coroutines.*
import org.zeromq.*

internal class PeerSocket(
    private val localType: Type,
    private val mailbox: PeerMailbox,
    socket: Socket,
) {
    private var input = socket.openReadChannel()
    private var output = socket.openWriteChannel(autoFlush = true)

    private var peerMinorVersion: Int = 1

    suspend fun handleInitialization(isServer: Boolean, peerSocketTypes: Set<Type>) {
        logger.t { "Writing greeting part 1" }
        output.writeGreetingPart1()

        logger.t { "Reading greeting part 1" }
        val peerMajorVersion = input.readGreetingPart1()
        if (peerMajorVersion != 3)
            protocolError("Incompatible version $peerMajorVersion.x")

        logger.t { "Writing greeting part 2" }
        output.writeGreetingPart2(Mechanism.NULL, false)

        logger.t { "Reading greeting part 2" }
        val (peerMinorVersion, peerSecuritySpec) = input.readGreetingPart2()

        val properties = when (peerSecuritySpec.mechanism) {
            Mechanism.NULL -> nullMechanismHandshake(localType, isServer, input, output)
            else -> protocolError("Unsupported mechanism ${peerSecuritySpec.mechanism}")
        }

        validateSocketType(properties, peerSocketTypes)

        this.peerMinorVersion = peerMinorVersion
        logger.t { "Finished initialization (peerMinorVersion: $peerMinorVersion)" }
    }

    suspend fun handleTraffic(): Unit = coroutineScope {
        launch {
            while (isActive) {
                readIncoming().let { mailbox.receiveChannel.send(it) }
            }
        }
        launch {
            while (isActive) {
                mailbox.sendChannel.receive().let { writeOutgoing(it) }
            }
        }
    }

    private suspend fun readIncoming(): CommandOrMessage {
        val raw = input.readCommandOrMessage()

        // Specially handle ZMTP 3.0 subscriptions
        val incoming = if (peerMinorVersion == 0 && localType == Type.PUB) {
            transformSubscriptionMessages(raw)
        } else raw

        logger.t { "Read: $incoming" }
        return incoming
    }

    private suspend fun writeOutgoing(outgoing: CommandOrMessage) {
        // Specially handle ZMTP 3.0 subscriptions
        val transformed = if (peerMinorVersion == 0 && localType == Type.SUB) {
            transformSubscriptionCommands(outgoing)
        } else outgoing

        output.writeCommandOrMessage(transformed)

        logger.t { "Wrote: $outgoing" }
    }
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

private fun validateSocketType(
    properties: Map<PropertyName, ByteArray>,
    peerSocketTypes: Set<Type>,
) {
    val socketTypeProperty = (properties[PropertyName.SOCKET_TYPE]
        ?: protocolError("No socket type property in metadata"))
    val peerSocketType = findSocketType(socketTypeProperty.decodeToString())
    if (!peerSocketTypes.contains(peerSocketType)) protocolError("Invalid socket type: $peerSocketType")
}

private fun findSocketType(socketTypeString: String): Type? =
    Type.values().find { it.name == socketTypeString.uppercase() }
