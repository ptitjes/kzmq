/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import de.infix.testBalloon.framework.*
import kotlin.test.*

val CircularQueueTests by testSuite {
    test("requires at least one element") {
        val queue = CircularQueue<Int>()
        assertFailsWith<IllegalStateException> { queue.rotate() }
    }

    test("rotates with a single element") {
        val queue = CircularQueue<Int>().apply { add(1) }
        repeat(10) {
            assertEquals(1, queue.rotate())
        }
    }

    test("rotates with multiple elements") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        repeat(3) {
            assertEquals(1, queue.rotate())
            assertEquals(2, queue.rotate())
            assertEquals(3, queue.rotate())
        }
    }

    test("remove current element") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.remove(1)
        assertEquals(listOf(2, 3), queue.elements)
    }

    test("remove other element") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.remove(2)
        assertEquals(listOf(1, 3), queue.elements)
    }

    test("rotates to original with rotate(count)") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotate(3)
        assertEquals(listOf(1, 2, 3), queue.elements)
    }

    test("rotates to original with rotateAfter(index)") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotateAfter(2)
        assertEquals(listOf(1, 2, 3), queue.elements)
    }

    test("remove last element after rotation") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotate(2)
        queue.remove(2)
        assertEquals(listOf(3, 1), queue.elements)
    }

    test("remove next element after rotation") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotate(2)
        queue.remove(3)
        assertEquals(listOf(1, 2), queue.elements)
    }
}
