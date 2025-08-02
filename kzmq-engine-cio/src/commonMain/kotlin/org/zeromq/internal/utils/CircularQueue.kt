/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import kotlinx.atomicfu.locks.*

internal class CircularQueue<T>() : List<T> {

    private val lock = reentrantLock()

    private val _elements: MutableList<T> = mutableListOf()
    private var currentIndex: Int = 0

    val elements
        get() = lock.withLock {
            _elements.subList(currentIndex, _elements.size) + _elements.subList(0, currentIndex)
        }

    fun add(element: T) = lock.withLock {
        _elements.add(currentIndex, element)
        currentIndex = (currentIndex + 1) % _elements.size
    }

    fun remove(element: T) = lock.withLock {
        val index = _elements.indexOf(element)
        if (index == -1) return

        _elements.removeAt(index)
        if (currentIndex > index) {
            currentIndex--
        }
    }

    fun rotate(): T = lock.withLock {
        check(_elements.isNotEmpty()) { "Queue is empty." }
        val element = _elements[currentIndex]
        currentIndex = (currentIndex + 1) % _elements.size
        return element
    }

    fun rotate(count: Int) = lock.withLock {
        check(_elements.isNotEmpty()) { "Queue is empty." }
        currentIndex = (currentIndex + count) % _elements.size
    }

    fun rotateAfter(index: Int) = lock.withLock {
        rotate(index + 1)
    }

    override val size: Int get() = lock.withLock { _elements.size }

    override fun isEmpty(): Boolean = lock.withLock { _elements.isEmpty() }

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
        return this.elements.containsAll(elements)
    }

    override fun contains(element: T): Boolean {
        return elements.contains(element)
    }
}
