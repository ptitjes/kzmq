/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

interface Frame {

    fun release()

    fun steal(): Frame

    val size: Int

    fun isEmpty() = size == 0

    fun getByteAt(index: Int): Byte

    fun copyOf(
        fromIndex: Int = 0,
        toIndex: Int = size,
    ): ByteArray

    fun copyInto(
        destination: ByteArray,
        destinationOffset: Int = 0,
        fromIndex: Int = 0,
        toIndex: Int = size,
    ): ByteArray

    fun read(reader: (array: ByteArray, offset: Int, length: Int) -> Unit)

    operator fun iterator(): ByteIterator {
        return object : ByteIterator() {
            var index = -1
            override fun hasNext(): Boolean = index < size - 1
            override fun nextByte(): Byte = getByteAt(++index)
        }
    }

    companion object {
        val EMPTY: Frame = ConstantByteArrayFrame(EMPTY_BYTE_ARRAY)
    }
}

operator fun Frame.get(index: Int): Byte = getByteAt(index)

fun Frame.isNotEmpty(): Boolean = !isEmpty()

fun constantFrameOf(array: ByteArray): Frame = ConstantByteArrayFrame(array)
fun constantFrameOf(string: String): Frame = ConstantByteArrayFrame(string.encodeToByteArray())

fun frameOf(array: ByteArray, offset: Int = 0, length: Int = array.size, free: (ByteArray) -> Unit): Frame =
    VariableByteArrayFrame(array, offset, length, free)

private val EMPTY_BYTE_ARRAY = ByteArray(0)

internal abstract class ByteArrayFrame : Frame {
    abstract val array: ByteArray
    abstract val offset: Int
    abstract val length: Int

    override val size: Int get() = length

    override fun getByteAt(index: Int): Byte = array[offset + index]

    override fun copyOf(fromIndex: Int, toIndex: Int): ByteArray =
        array.copyOfRange(fromIndex + offset, toIndex + offset)

    override fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray =
        array.copyInto(destination, destinationOffset, fromIndex, toIndex)

    override fun read(reader: (array: ByteArray, offset: Int, length: Int) -> Unit) {
        reader(array, offset, length)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ByteArrayFrame

        if (!array.contentEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
        return array.contentHashCode()
    }

    override fun toString(): String {
        return "ByteArrayFrame(array=${array.contentToString()}, offset=$offset, length=$length)"
    }
}

private class ConstantByteArrayFrame(
    override val array: ByteArray,
    override val offset: Int = 0,
    override val length: Int = array.size,
) : ByteArrayFrame() {
    override fun release() {}
    override fun steal(): Frame = this
}

private class VariableByteArrayFrame(
    array: ByteArray,
    offset: Int = 0,
    length: Int = array.size,
    private val free: (ByteArray) -> Unit = {},
) : ByteArrayFrame() {

    override var array = array
        private set

    override var offset = offset
        private set

    override var length = length
        private set

    override fun steal(): Frame = VariableByteArrayFrame(array, offset, length, free).also {
        array = EMPTY_BYTE_ARRAY
        offset = 0
        length = 0
    }

    override fun release() {
        free(array)
        array = EMPTY_BYTE_ARRAY
        offset = 0
        length = 0
    }
}
