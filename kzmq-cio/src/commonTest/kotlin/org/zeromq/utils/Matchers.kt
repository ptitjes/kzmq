/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.utils

import io.kotest.matchers.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.*
import org.zeromq.internal.*
import org.zeromq.test.*
import kotlin.jvm.*
import kotlin.time.Duration.Companion.seconds

@JvmName("messageChannelShouldReceiveExactly")
internal suspend infix fun ReceiveChannel<Message>.shouldReceiveExactly(expected: List<MessageTemplate>) {
    shouldReceiveExactly(expected) { receive() }
}

@JvmName("commandOrMessageChannelShouldReceiveExactly")
internal suspend infix fun ReceiveChannel<CommandOrMessage>.shouldReceiveExactly(expected: List<MessageTemplate>) {
    shouldReceiveExactly(expected) { receive().messageOrThrow() }
}

@JvmName("messageChannelSend")
internal suspend fun SendChannel<Message>.send(template: MessageTemplate) =
    send(buildMessage { template.frames.forEach { writeFrame(it) } })

@JvmName("commandOrMessageChannelSend")
internal suspend fun SendChannel<CommandOrMessage>.send(template: MessageTemplate) =
    send(CommandOrMessage(buildMessage { template.frames.forEach { writeFrame(it) } }))

internal suspend infix fun (suspend () -> Message).shouldReceiveExactly(expected: List<MessageTemplate>) {
    shouldReceiveExactly(expected) { this() }
}

internal suspend fun (suspend () -> Message).shouldReceiveNothing() {
    withTimeoutOrNull(1.seconds) {
        this@shouldReceiveNothing()
    } shouldBe null
}

internal suspend infix fun (suspend (Message) -> Unit).send(expected: MessageTemplate) {
    this(expected.buildMessage())
}
