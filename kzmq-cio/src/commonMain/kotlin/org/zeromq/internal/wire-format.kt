/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package org.zeromq.internal

import kotlin.jvm.*

internal const val NULL: UByte = 0x00u

internal const val signatureHeadByte: UByte = 0xffu
internal const val signatureTrailByte: UByte = 0x7fu

internal const val MAJOR_VERSION = 3
internal const val MINOR_VERSION = 1

internal val SIGNATURE =
    ubyteArrayOf(
        signatureHeadByte,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        signatureTrailByte
    )

internal enum class Mechanism {
    NULL,
    PLAIN,
    CURVE;

    val bytes: ByteArray = name.encodeToByteArray()
}

internal const val AS_CLIENT: UByte = 0x00u
internal const val AS_SERVER: UByte = 0x01u

internal val FILLER = UByteArray(31) { NULL }

internal const val MECHANISM_SIZE = 20

private const val FLAG_MORE: UByte = 0x01u
private const val FLAG_LONG_SIZE: UByte = 0x02u
private const val FLAG_COMMAND: UByte = 0x04u

@JvmInline
internal value class ZmqFlags(val data: UByte) {
    val isMore: Boolean get() = data and FLAG_MORE != NULL
    val isLongSize: Boolean get() = data and FLAG_LONG_SIZE != NULL
    val isCommand: Boolean get() = data and FLAG_COMMAND != NULL

    operator fun plus(other: ZmqFlags) = ZmqFlags(this.data or other.data)

    companion object {
        val none = ZmqFlags(NULL)
        val more = ZmqFlags(FLAG_MORE)
        val longSize = ZmqFlags(FLAG_LONG_SIZE)
        val command = ZmqFlags(FLAG_COMMAND)
    }
}
