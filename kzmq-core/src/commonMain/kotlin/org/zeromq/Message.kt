/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

fun messageOf(frames: List<Frame>) = Message(frames)
fun messageOf(vararg frames: Frame) = Message(frames.toList())

class Message(frames: List<Frame> = emptyList()) {

    private val _frames: MutableList<Frame> = mutableListOf()

    init {
        _frames.addAll(frames.map { it.steal() })
    }

    val frames: List<Frame> get() = _frames

    fun release() {
        _frames.forEach { it.release() }
        _frames.clear()
    }

    fun steal(): Message = Message(frames.map { it.steal() }).also {
        _frames.clear()
    }

    val isSingle: Boolean get() = frames.size == 1
    val isMultipart: Boolean get() = frames.size > 1

    fun peekFirst(): Frame = frames.getOrNull(0) ?: error("Message has no parts")

    fun pushFirst(frame: Frame) {
        _frames.add(0, frame)
    }

    fun removeFirst(): Frame = _frames.removeFirst()
    fun removeFirstOrNull(): Frame? = _frames.removeFirstOrNull()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Message

        if (frames != other.frames) return false

        return true
    }

    override fun hashCode(): Int {
        return frames.hashCode()
    }

    override fun toString(): String {
        return "Message(frames=$frames)"
    }
}

fun Message.isEmpty() = frames.isEmpty()
fun Message.isNotEmpty() = frames.isNotEmpty()

fun Message.removeFirstWhile(predicate: (frame: Frame) -> Boolean): List<Frame> {
    val removed = mutableListOf<Frame>()
    while (predicate(peekFirst())) {
        removed += removeFirst()
    }
    return removed
}
