/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

internal class CircularQueue<T> private constructor(
    private val _elements: MutableList<T>,
) : List<T> by _elements {

    constructor() : this(mutableListOf())

    fun add(element: T) {
        _elements.add(element)
    }

    fun remove(element: T) {
        _elements.remove(element)
    }

    fun rotate(): T {
        check(isNotEmpty()) { "Queue is empty." }
        val mailbox = _elements.removeFirst()
        _elements += mailbox
        return mailbox
    }
}

internal fun CircularQueue<*>.rotateAfter(index: Int) {
    repeat(index + 1) { rotate() }
}
