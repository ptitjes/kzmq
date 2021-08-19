package org.zeromq

data class Message(val parts: List<ByteArray>) {

    constructor(vararg parts: ByteArray) : this(parts.toList())

    val isSingle: Boolean get() = parts.size == 1
    val isMultipart: Boolean get() = parts.size > 1

    fun singleOrThrow(): ByteArray =
        if (isSingle) parts[0] else error("Message is multipart")
}
