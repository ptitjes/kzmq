/*
 * Copyright (c) 2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.test

import kotlinx.io.*
import kotlinx.io.bytestring.*
import org.zeromq.*

public data class MessageTemplate(val frames: List<ByteString>)

public fun MessageTemplate.buildMessage(): Message = Message().apply {
    frames.forEach { writeFrame(it) }
}

public fun messages(count: Int, writer: WriteScope.(index: Int) -> Unit): List<MessageTemplate> {
    return List(count) { index -> message { writer(index) } }
}

public fun message(writer: WriteScope.() -> Unit): MessageTemplate =
    MessageTemplate(TemplateWriteScope().apply { writer() }.frames)

private class TemplateWriteScope : WriteScope {
    val frames = mutableListOf<ByteString>()

    override fun writeFrame(source: Buffer) {
        frames += source.readByteString()
    }
}

public fun message(frame: ByteString): MessageTemplate = message { writeFrame(frame) }

public suspend fun SendSocket.send(message: MessageTemplate): Unit = send(message.buildMessage())
