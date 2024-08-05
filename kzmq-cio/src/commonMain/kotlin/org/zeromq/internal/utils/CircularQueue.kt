/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

internal class CircularQueue<T> private constructor(
    private val _elements: MutableList<T>,
) : List<T> {

    private var currentIndex: Int = 0

    constructor() : this(mutableListOf())

    val elements get() = _elements.subList(currentIndex, _elements.size) + _elements.subList(0, currentIndex)

    fun add(element: T) {
        _elements.add(currentIndex, element)
        currentIndex = (currentIndex + 1) % _elements.size
    }

    fun remove(element: T) {
        val index = _elements.indexOf(element)
        if (index == -1) return

        _elements.removeAt(index)
        if (currentIndex > index) {
            currentIndex--
        }
    }

    fun rotate(): T {
        check(_elements.isNotEmpty()) { "Queue is empty." }
        val element = _elements[currentIndex]
        currentIndex = (currentIndex + 1) % _elements.size
        return element
    }

    fun rotate(count: Int) {
        check(_elements.isNotEmpty()) { "Queue is empty." }
        currentIndex = (currentIndex + count) % elements.size
    }

    fun rotateAfter(index: Int) {
        rotate(index + 1)
    }

    override val size: Int get() = _elements.size

    override fun isEmpty(): Boolean {
        return _elements.isEmpty()
    }

    override fun get(index: Int): T {
        return elements[index]
    }

    override fun iterator(): Iterator<T> {
        return elements.iterator()
    }

    override fun listIterator(): ListIterator<T> {
        return elements.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<T> {
        return elements.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        return elements.subList(fromIndex, toIndex)
    }

    override fun lastIndexOf(element: T): Int {
        return elements.lastIndexOf(element)
    }

    override fun indexOf(element: T): Int {
        return elements.indexOf(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return _elements.containsAll(elements)
    }

    override fun contains(element: T): Boolean {
        return _elements.contains(element)
    }
}
