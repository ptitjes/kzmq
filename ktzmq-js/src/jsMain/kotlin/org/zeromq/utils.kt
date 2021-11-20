package org.zeromq

import Buffer
import org.khronos.webgl.*

fun ByteArray.toBuffer(): Buffer {
    val int8Array: Int8Array = this.unsafeCast<Int8Array>()
    return Uint8Array(
        int8Array.buffer,
        int8Array.byteOffset,
        int8Array.byteLength
    ).unsafeCast<Buffer>()
}

fun Buffer.toByteArray(): ByteArray {
    val uint8Array: Uint8Array = this.unsafeCast<Uint8Array>()
    return Int8Array(
        uint8Array.buffer,
        uint8Array.byteOffset,
        uint8Array.byteLength
    ).unsafeCast<ByteArray>()
}
