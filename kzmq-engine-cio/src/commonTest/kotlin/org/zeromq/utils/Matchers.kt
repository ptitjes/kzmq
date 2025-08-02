/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*
import org.zeromq.test.*
import kotlin.jvm.*

internal suspend fun assertReceivesExactly(expected: List<Message>, channel: ReceiveChannel<CommandOrMessage>) {
    assertReceivesExactly(expected) { channel.receive().messageOrThrow() }
}

internal fun assertHasReceivedExactly(expected: List<Message>, channel: ReceiveChannel<CommandOrMessage>) {
    assertHasReceivedExactly(expected) { channel.tryReceive().getOrNull()?.messageOrThrow() }
}

internal suspend fun assertReceivesNothing(channel: ReceiveChannel<CommandOrMessage>) {
    assertSuspends { channel.receive() }
}

internal suspend fun assertReceivesNothing(tryReceive: suspend () -> Message) {
    assertSuspends { tryReceive() }
}

@JvmName("commandOrMessageChannelSend")
internal suspend fun SendChannel<CommandOrMessage>.send(message: Message) = send(CommandOrMessage(message))
