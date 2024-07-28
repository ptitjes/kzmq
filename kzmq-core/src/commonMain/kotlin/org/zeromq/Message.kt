/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ message container. Messages carry application data and are not generally created, modified,
 * or filtered by the ZMTP implementation except in some cases. Messages consist of one or more frames
 * and are always sent and delivered atomically, that is, all the frames of a message, or none of them.
 *
 * @param frames the frames of the message.
 */
public data class Message(val frames: List<ByteArray>) {
    init {
        require(frames.isNotEmpty()) { "A message should contain at least one frame" }
    }

    /**
     * Builds a ZeroMQ message.
     *
     * @param frames the frames of the message.
     */
    public constructor(vararg frames: ByteArray) : this(frames.toList())

    /**
     * Returns `true` if this message contains a single frame.
     */
    val isSingle: Boolean get() = frames.size == 1

    /**
     * Returns `true` if this message contains more than one frame.
     */
    val isMultipart: Boolean get() = frames.size > 1

    /**
     * Returns the single frame of this message, or throws if this message is multipart.
     */
    public fun singleOrThrow(): ByteArray = if (isSingle) frames[0] else error("Message is multipart")

    /**
     * Returns the first frame.
     */
    public fun first(): ByteArray = frames.first()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Message

        if (frames.size != other.frames.size) return false
        for (i in frames.indices) {
            if (!frames[i].contentEquals(other.frames[i])) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        for (frame in frames) {
            result = 31 * result + frame.contentHashCode()
        }
        return result
    }

    override fun toString(): String {
        return "Message(parts=${frames.joinToString { it.contentToString() }})"
    }
}
