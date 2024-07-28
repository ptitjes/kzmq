/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import Buffer
import org.khronos.webgl.*
import kotlin.properties.*
import kotlin.reflect.*

internal fun ByteArray.toBuffer(): Buffer {
    val int8Array: Int8Array = this.unsafeCast<Int8Array>()
    return Uint8Array(
        int8Array.buffer,
        int8Array.byteOffset,
        int8Array.byteLength
    ).unsafeCast<Buffer>()
}

internal fun Buffer.toByteArray(): ByteArray {
    val uint8Array: Uint8Array = this.unsafeCast<Uint8Array>()
    return Int8Array(
        uint8Array.buffer,
        uint8Array.byteOffset,
        uint8Array.byteLength
    ).unsafeCast<ByteArray>()
}

internal fun <R> KMutableProperty0<String?>.asNullableByteArrayProperty(): ReadWriteProperty<R, ByteArray?> {
    return object : ReadWriteProperty<R, ByteArray?> {
        override fun getValue(thisRef: R, property: KProperty<*>): ByteArray? =
            this@asNullableByteArrayProperty.get()?.encodeToByteArray()

        override fun setValue(thisRef: R, property: KProperty<*>, value: ByteArray?) =
            this@asNullableByteArrayProperty.set(value?.decodeToString())
    }
}
