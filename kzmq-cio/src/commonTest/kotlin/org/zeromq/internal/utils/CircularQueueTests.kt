/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

@Suppress("unused")
class CircularQueueTests : FunSpec({

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
})
