/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import kotlinx.coroutines.selects.*
import org.zeromq.*
import org.zeromq.internal.*

internal fun CircularQueue<PeerMailbox>.update(event: PeerEvent) {
    val mailbox = event.peerMailbox
    when (event.kind) {
        PeerEvent.Kind.ADDITION -> add(mailbox)
        PeerEvent.Kind.REMOVAL -> remove(mailbox)
        else -> {}
    }
}

internal fun CircularQueue<PeerMailbox>.updateOnConnection(event: PeerEvent) {
    val mailbox = event.peerMailbox
    when (event.kind) {
        PeerEvent.Kind.CONNECTION -> add(mailbox)
        PeerEvent.Kind.DISCONNECTION -> remove(mailbox)
        else -> {}
    }
}

internal suspend fun CircularQueue<PeerMailbox>.sendToFirstAvailable(message: Message): PeerMailbox? {
    // Fast path: Find the first mailbox we can send immediately
    logger.v { "Try sending message $message to first available" }
    var targetMailbox = trySendToFirstAvailable(message)

    if (targetMailbox == null) {
        // Slow path: Biased select on each mailbox's onSend
        logger.v { "Sending message $message to first available" }
        select<Unit> {
            val commandOrMessage = CommandOrMessage(message)
            forEachIndexed { index, mailbox ->
                mailbox.sendChannel.onSend(commandOrMessage) {
                    logger.v { "Sent message to $mailbox" }
                    rotateAfter(index)
                    targetMailbox = mailbox
                }
            }
        }
    }

    return targetMailbox
}

internal fun CircularQueue<PeerMailbox>.trySendToFirstAvailable(message: Message): PeerMailbox? {
    val commandOrMessage = CommandOrMessage(message)
    val index = indexOfFirst { mailbox ->
        val result = mailbox.sendChannel.trySend(commandOrMessage)
        logger.v {
            if (result.isSuccess) "Sent message to $mailbox"
            else "Failed to send message to $mailbox"
        }
        result.isSuccess
    }

    val targetMailbox = if (index != -1) getOrNull(index) else null
    if (targetMailbox != null) rotateAfter(index)
    return targetMailbox
}

internal suspend fun CircularQueue<PeerMailbox>.receiveFromFirst(): Pair<PeerMailbox, Message> {
    return select {
        forEachIndexed { index, mailbox ->
            mailbox.receiveChannel.onReceive { commandOrMessage ->
                val message = commandOrMessage.messageOrThrow()
                logger.v { "Received $message from $mailbox" }
                rotateAfter(index)
                mailbox to message
            }
        }
    }
}
