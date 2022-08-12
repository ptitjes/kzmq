/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public data class Message(val frames: List<ByteArray>) {
    init {
        require(frames.isNotEmpty()) { "A message should contain at least one frame" }
    }

    public constructor(vararg frames: ByteArray) : this(frames.toList())

    val isSingle: Boolean get() = frames.size == 1
    val isMultipart: Boolean get() = frames.size > 1

    public fun singleOrThrow(): ByteArray =
        if (isSingle) frames[0] else error("Message is multipart")

    public fun firstOrThrow(): ByteArray =
        frames.getOrNull(0) ?: error("Message contains no frame")

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
