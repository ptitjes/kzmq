/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

data class Message(val parts: List<ByteArray>) {
    init {
        require(parts.isNotEmpty()) { "parts should contain at least one part" }
    }

    constructor(vararg parts: ByteArray) : this(parts.toList())

    val isSingle: Boolean get() = parts.size == 1
    val isMultipart: Boolean get() = parts.size > 1

    fun singleOrThrow(): ByteArray =
        if (isSingle) parts[0] else error("Message is multipart")

    fun firstOrThrow(): ByteArray =
        parts.getOrNull(0) ?: error("Message has no parts")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Message

        if (parts.size != other.parts.size) return false
        for (i in parts.indices) {
            if (!parts[i].contentEquals(other.parts[i])) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result: Int = 0
        for (part in parts) {
            result = 31 * result + part.contentHashCode()
        }
        return result
    }

    override fun toString(): String {
        return "Message(parts=${parts.joinToString { it.contentToString() }})"
    }
}
