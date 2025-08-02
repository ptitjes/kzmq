/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import de.infix.testBalloon.framework.*
import io.kotest.assertions.throwables.*
import io.kotest.matchers.*
import io.kotest.matchers.equals.*

val CircularQueueTests by testSuite {
    test("requires at least one element") {
        val queue = CircularQueue<Int>()
        shouldThrow<IllegalStateException> { queue.rotate() }
    }

    test("rotates with a single element") {
        val queue = CircularQueue<Int>().apply { add(1) }
        repeat(10) {
            queue.rotate() shouldBe 1
        }
    }

    test("rotates with multiple elements") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        repeat(3) {
            queue.rotate() shouldBe 1
            queue.rotate() shouldBe 2
            queue.rotate() shouldBe 3
        }
    }

    test("remove current element") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.remove(1)
        queue.elements shouldBeEqual listOf(2, 3)
    }

    test("remove other element") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.remove(2)
        queue.elements shouldBeEqual listOf(1, 3)
    }

    test("rotates to original with rotate(count)") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotate(3)
        queue.elements shouldBeEqual listOf(1, 2, 3)
    }

    test("rotates to original with rotateAfter(index)") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotateAfter(2)
        queue.elements shouldBeEqual listOf(1, 2, 3)
    }

    test("remove last element after rotation") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotate(2)
        queue.remove(2)
        queue.elements shouldBeEqual listOf(3, 1)
    }

    test("remove next element after rotation") {
        val queue = CircularQueue<Int>().apply { add(1); add(2); add(3) }
        queue.rotate(2)
        queue.remove(3)
        queue.elements shouldBeEqual listOf(1, 2)
    }
}
