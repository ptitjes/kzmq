/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.*
import kotlinx.io.bytestring.*

/**
 * A ZeroMQ message container. Messages carry application data and are not generally created, modified,
 * or filtered by the ZMTP implementation except in some cases. Messages consist of one or more frames
 * and are always sent and delivered atomically, that is, all the frames of a message, or none of them.
 *
 * @param frames the frames of the message.
 */
public class Message(private var frames: List<Buffer>) : ReadScope, WriteScope {
    /**
     * Builds a ZeroMQ message.
     *
     * @param frames the frames of the message.
     */
    public constructor(vararg frames: Buffer) : this(frames.toList())

    /**
     * Returns `true` if this message contains a single frame.
     */
    public val isSingle: Boolean get() = frames.size == 1

    /**
     * Returns `true` if this message contains more than one frame.
     */
    public val isMultipart: Boolean get() = frames.size > 1

    /**
     * Returns the single frame of this message, or throws if this message is multipart.
     */
    public fun singleOrThrow(): Source {
        if (!isSingle) error("Message is multipart")
        return readFrame()
    }

    /**
     * Peeks the first frame.
     */
    public fun peekFirstFrame(): Source = frames.first().copy().peek()

    public override fun readFrame(): Buffer {
        val frame = frames.first()
        this.frames = frames.drop(1)
        return frame
    }

    override fun ignoreRemainingFrames() {
        readFrames().forEach { frame -> frame.skip(frame.size) }
    }

    override fun ensureNoRemainingFrames() {
        if (frames.isNotEmpty()) error("Remaining ${frames.size} frame(s) in $this")
    }

    public fun readFrames(): List<Buffer> {
        val frames = this.frames
        this.frames = listOf()
        return frames
    }

    public override fun writeFrame(source: Buffer) {
        frames += source
    }

    public fun writeFrames(sources: List<Buffer>) {
        frames += sources
    }

    public fun copy(): Message {
        return Message(frames.map { it.copy() })
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "Message(frames=${frames.joinToString { it.copy().readByteString().toHexString() }})"
    }
}

public fun Message(frames: List<ByteString>): Message = Message(frames.map { Buffer().apply { write(it) } })

public fun Message(frame: ByteString): Message = Message(listOf(frame))

public fun Message(frame: String): Message = Message(listOf(frame.encodeToByteString()))

public fun buildMessage(writer: WriteScope.() -> Unit): Message = Message().apply(writer)
