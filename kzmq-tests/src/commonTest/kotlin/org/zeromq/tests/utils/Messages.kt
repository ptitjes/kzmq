/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("TestFunctionName")

package org.zeromq.tests.utils

fun Short.encodeToByteArray(): ByteArray = byteArrayOf(highByte, lowByte)
fun ByteArray.decodeToShort(): Short = Short(this[0], this[1])

fun Int.encodeToByteArray(): ByteArray {
    val highShort = highShort
    val lowShort = lowShort
    return byteArrayOf(highShort.highByte, highShort.lowByte, lowShort.highByte, lowShort.lowByte)
}

fun ByteArray.decodeToInt(): Int = Int(Short(this[0], this[1]), Short(this[2], this[3]))

inline val Short.highByte: Byte get() = (toInt() ushr 8).toByte()
inline val Short.lowByte: Byte get() = (toInt() and 0xff).toByte()

inline val Int.highShort: Short get() = (this ushr 16).toShort()
inline val Int.lowShort: Short get() = (this and 0xffff).toShort()

internal fun Short(highByte: Byte, lowByte: Byte): Short =
    ((highByte.toInt() shl 8) or (lowByte.toInt() and 0xff)).toShort()

internal fun Int(highShort: Short, lowShort: Short): Int =
    (highShort.toInt() shl 16) or (lowShort.toInt() and 0xffff)
