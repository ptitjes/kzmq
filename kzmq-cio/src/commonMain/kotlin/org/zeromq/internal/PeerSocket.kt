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
    private val socketOptions: SocketOptions,
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

        val localProperties = mutableMapOf<PropertyName, ByteArray>().apply {
            put(PropertyName.SOCKET_TYPE, localType.name.encodeToByteArray())
            socketOptions.routingId?.let { identity -> put(PropertyName.IDENTITY, identity) }
        }

        val peerProperties = when (peerSecuritySpec.mechanism) {
            Mechanism.NULL -> nullMechanismHandshake(localProperties, isServer, input, output)
            else -> protocolError("Unsupported mechanism ${peerSecuritySpec.mechanism}")
        }

        validateSocketType(peerProperties, peerSocketTypes)

        val identity = peerProperties[PropertyName.IDENTITY]?.let { Identity(it) }
        if (mailbox.identity != null && mailbox.identity != identity) {
            logger.e { "Identity mismatch: old=${mailbox.receiveChannel} new=$identity)" }
        }
        mailbox.identity = identity

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

    private val isPublisher: Boolean get() = localType == Type.PUB || localType == Type.XPUB
    private val isSubscriber: Boolean get() = localType == Type.SUB || localType == Type.XSUB

    private suspend fun readIncoming(): CommandOrMessage {
        val raw = input.readCommandOrMessage()

        // Specially handle ZMTP 3.0 subscriptions
        val incoming = if (peerMinorVersion == 0 && isPublisher) {
            transformSubscriptionMessages(raw)
        } else raw

        logger.t { "Read: $incoming" }
        return incoming
    }

    private suspend fun writeOutgoing(outgoing: CommandOrMessage) {
        // Specially handle ZMTP 3.0 subscriptions
        val transformed = if (peerMinorVersion == 0 && isSubscriber) {
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
    return destructureSubscriptionMessage(message)?.let { (subscribe, topic) ->
        CommandOrMessage(
            if (subscribe) SubscribeCommand(topic) else CancelCommand(topic)
        )
    }
}

private fun transformSubscriptionCommands(commandOrMessage: CommandOrMessage): CommandOrMessage =
    if (commandOrMessage.isCommand) {
        when (val command = commandOrMessage.commandOrThrow()) {
            is SubscribeCommand ->
                CommandOrMessage(subscriptionMessageOf(true, command.topic))

            is CancelCommand ->
                CommandOrMessage(subscriptionMessageOf(false, command.topic))

            else -> commandOrMessage
        }
    } else commandOrMessage

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
