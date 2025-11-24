/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import kotlinx.coroutines.*
import org.zeromq.*
import org.zeromq.internal.*

internal data class Receipt(
    val sourceMailbox: PeerMailbox,
    val commandOrMessage: CommandOrMessage,
)

internal fun CircularQueue<PeerMailbox>.updateOnAdditionRemoval(event: PeerEvent) {
    val mailbox = event.peerMailbox
    when (event.kind) {
        PeerEvent.Kind.ADDITION -> add(mailbox)
        PeerEvent.Kind.REMOVAL -> remove(mailbox)
        else -> {}
    }
}

internal fun CircularQueue<PeerMailbox>.updateOnConnectionDisconnection(event: PeerEvent) {
    val mailbox = event.peerMailbox
    when (event.kind) {
        PeerEvent.Kind.CONNECTION -> add(mailbox)
        PeerEvent.Kind.DISCONNECTION -> remove(mailbox)
        else -> {}
    }
}

internal suspend fun CircularQueue<PeerMailbox>.sendToFirstAvailable(message: Message): PeerMailbox {
    while (true) {
        val maybeMailbox = trySendToFirstAvailable(message)
        if (maybeMailbox != null) return maybeMailbox
        yield()
    }
}

internal fun CircularQueue<PeerMailbox>.trySendToFirstAvailable(message: Message): PeerMailbox? {
    val commandOrMessage = CommandOrMessage(message)
    val index = indexOfFirst { mailbox ->
        val result = mailbox.sendChannel.trySend(commandOrMessage)
        if (result.isSuccess) logger.v { "Sent message to $mailbox" }
        result.isSuccess
    }

    val targetMailbox = if (index != -1) getOrNull(index) else null
    if (targetMailbox != null) rotateAfter(index)
    return targetMailbox
}

internal suspend fun CircularQueue<PeerMailbox>.receiveFromFirst(): Receipt {
    logger.v { "Receive from first" }
    while (true) {
        val maybeReceipt = tryReceiveFromFirst()
        if (maybeReceipt != null) {
            logger.v { "Receive ${maybeReceipt.commandOrMessage} from ${maybeReceipt.sourceMailbox}" }
            return maybeReceipt
        }
        yield()
    }
}

internal fun CircularQueue<PeerMailbox>.tryReceiveFromFirst(): Receipt? {
    var received: CommandOrMessage? = null
    val index = indexOfFirst { mailbox ->
        val result = mailbox.receiveChannel.tryReceive()
        if (result.isSuccess) {
            received = result.getOrThrow()
            logger.v { "Received message from $mailbox" }
        }
        result.isSuccess
    }

    val targetMailbox = if (index != -1) getOrNull(index) else null
    if (targetMailbox != null) rotateAfter(index)
    return targetMailbox?.let { Receipt(it, received!!) }
}
