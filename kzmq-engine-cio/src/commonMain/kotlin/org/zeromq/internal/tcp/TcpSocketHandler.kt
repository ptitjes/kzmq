/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.tcp

import io.ktor.network.sockets.*
import io.ktor.network.sockets.Socket
import kotlinx.coroutines.*
import kotlinx.io.bytestring.*
import org.zeromq.*
import org.zeromq.internal.*

internal class TcpSocketHandler(
    private val socketInfo: SocketInfo,
    private val isServer: Boolean,
    private val mailbox: PeerMailbox,
    private val rawSocket: Socket,
) {
    private val connection = rawSocket.connection()
    private var input = connection.input
    private var output = connection.output

    private var peerMinorVersion: Int = 1

    suspend fun handleInitialization() {
        logger.v { "Writing greeting part 1" }
        output.writeGreetingPart1()

        logger.v { "Reading greeting part 1" }
        val peerMajorVersion = input.readGreetingPart1()
        if (peerMajorVersion != 3) protocolError("Incompatible peer version $peerMajorVersion.x")

        logger.v { "Writing greeting part 2" }
        val mechanism = socketInfo.options.getSelectedSecurityMechanism()
        output.writeGreetingPart2(mechanism, false)

        logger.v { "Reading greeting part 2" }
        val (peerMinorVersion, peerSecuritySpec) = input.readGreetingPart2()

        if (mechanism != peerSecuritySpec.mechanism)
            protocolError("Invalid peer security mechanism: ${peerSecuritySpec.mechanism}")

        val localProperties = mutableMapOf<PropertyName, ByteString>().apply {
            put(PropertyName.SOCKET_TYPE, socketInfo.type.name.encodeToByteString())
            socketInfo.options.routingId?.let { identity -> put(PropertyName.IDENTITY, identity) }
        }

        val peerProperties = when (mechanism) {
            Mechanism.NULL -> nullMechanismHandshake(localProperties, isServer, input, output)
            else -> TODO("Security mechanism $mechanism not yet supported")
        }

        validateSocketType(peerProperties, socketInfo.validPeerTypes)

        val identity = peerProperties[PropertyName.IDENTITY]?.let { Identity(it) }
        if (mailbox.identity != null && mailbox.identity != identity) {
            logger.e { "Identity mismatch: old=${mailbox.receiveChannel} new=$identity)" }
        }
        mailbox.identity = identity

        this.peerMinorVersion = peerMinorVersion
        logger.v { "Finished initialization (peerMinorVersion: $peerMinorVersion)" }
    }

    suspend fun handleTraffic() {
        try {
            logger.d { "Started handling traffic" }
            coroutineScope {
                launch {
                    val incoming = mailbox.receiveChannel
                    while (isActive) {
                        val incomingMessage = readIncoming()
                        logger.v { "(TCP: $mailbox) Incoming message: $incomingMessage" }
                        incoming.send(incomingMessage)
                    }
                }
                launch {
                    val outgoing = mailbox.sendChannel
                    while (isActive) {
                        val outgoingMessage = outgoing.receive()
                        logger.v { "(TCP: $mailbox) Outgoing message: $outgoingMessage" }
                        writeOutgoing(outgoingMessage)
                    }
                }
            }
        } finally {
            logger.d { "Stopped handling traffic" }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun handleLinger() {
        withTimeout(socketInfo.options.lingerTimeout) {
            try {
                logger.d { "Started lingering" }
                val outgoing = mailbox.sendChannel
                while (!outgoing.isClosedForReceive) writeOutgoing(outgoing.receive())
            } finally {
                logger.d { "Stopped lingering" }
            }
        }
    }

    fun close() {
        rawSocket.close()
    }

    private val isPublisher: Boolean get() = socketInfo.type == Type.PUB || socketInfo.type == Type.XPUB
    private val isSubscriber: Boolean get() = socketInfo.type == Type.SUB || socketInfo.type == Type.XSUB

    private suspend fun readIncoming(): CommandOrMessage {
        val raw = input.readCommandOrMessage()

        // Specially handle ZMTP 3.0 subscriptions
        val incoming = if (peerMinorVersion == 0 && isPublisher) {
            transformSubscriptionMessages(raw)
        } else raw

        return incoming
    }

    private suspend fun writeOutgoing(outgoing: CommandOrMessage) {
        // Specially handle ZMTP 3.0 subscriptions
        val transformed = if (peerMinorVersion == 0 && isSubscriber) {
            transformSubscriptionCommands(outgoing)
        } else outgoing

        output.writeCommandOrMessage(transformed)
    }
}

private fun transformSubscriptionMessages(commandOrMessage: CommandOrMessage): CommandOrMessage =
    if (commandOrMessage.isMessage) {
        extractSubscriptionCommand(commandOrMessage.messageOrThrow()) ?: commandOrMessage
    } else commandOrMessage

private fun extractSubscriptionCommand(message: Message): CommandOrMessage? {
    return message.toSubscriptionMessage()?.let {
        it.subscribe to it.topic
    }?.let { (subscribe, topic) ->
        CommandOrMessage(
            if (subscribe) SubscribeCommand(topic) else CancelCommand(topic)
        )
    }
}

private fun transformSubscriptionCommands(commandOrMessage: CommandOrMessage): CommandOrMessage =
    if (commandOrMessage.isCommand) {
        when (val command = commandOrMessage.commandOrThrow()) {
            is SubscribeCommand -> CommandOrMessage(SubscriptionMessage(true, command.topic).toMessage())

            is CancelCommand -> CommandOrMessage(SubscriptionMessage(false, command.topic).toMessage())

            else -> commandOrMessage
        }
    } else commandOrMessage

private fun validateSocketType(
    properties: Map<PropertyName, ByteString>,
    peerSocketTypes: Set<Type>,
) {
    val socketTypeProperty =
        (properties[PropertyName.SOCKET_TYPE] ?: protocolError("No socket type property in metadata"))
    val peerSocketType = findSocketType(socketTypeProperty.decodeToString())
    if (!peerSocketTypes.contains(peerSocketType)) protocolError("Invalid socket type: $peerSocketType")
}

private fun findSocketType(socketTypeString: String): Type? =
    Type.entries.find { it.name == socketTypeString.uppercase() }
