package org.zeromq

class Message(vararg val parts: ByteArray) {

    constructor(parts: List<ByteArray>) : this(*parts.toTypedArray())

    val isSingle: Boolean get() = parts.size == 1
    val isMultipart: Boolean get() = parts.size > 1

    fun singleOrThrow(): ByteArray =
        if (isSingle) parts[0] else error("Message is multipart")
}
