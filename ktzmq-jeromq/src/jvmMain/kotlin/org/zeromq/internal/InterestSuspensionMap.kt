/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.atomicfu.*
import kotlinx.coroutines.*

internal class InterestSuspensionsMap {

    private val readHandler = atomic<CancellableContinuation<Unit>?>(null)
    private val writeHandler = atomic<CancellableContinuation<Unit>?>(null)
    private val errorHandler = atomic<CancellableContinuation<Unit>?>(null)

    fun addSuspension(interest: SelectInterest, continuation: CancellableContinuation<Unit>) {
        if (!when (interest) {
                SelectInterest.READ -> readHandler.compareAndSet(null, continuation)
                SelectInterest.WRITE -> writeHandler.compareAndSet(null, continuation)
                SelectInterest.ERROR -> errorHandler.compareAndSet(null, continuation)
            }
        ) {
            throw IllegalStateException("Handler for ${interest.name} is already registered")
        }
    }

    @Suppress("LoopToCallChain")
    inline fun invokeForEachPresent(
        readyOps: Int,
        block: CancellableContinuation<Unit>.() -> Unit
    ) {
        for (interest in SelectInterest.AllInterests) {
            if (interest.flag and readyOps != 0) {
                removeSuspension(interest)?.block()
            }
        }
    }

    inline fun invokeForEachPresent(block: CancellableContinuation<Unit>.(SelectInterest) -> Unit) {
        for (interest in SelectInterest.AllInterests) {
            removeSuspension(interest)?.run { block(interest) }
        }
    }

    fun removeSuspension(interest: SelectInterest): CancellableContinuation<Unit>? =
        when (interest) {
            SelectInterest.READ -> readHandler.getAndSet(null)
            SelectInterest.WRITE -> writeHandler.getAndSet(null)
            SelectInterest.ERROR -> errorHandler.getAndSet(null)
        }

    override fun toString(): String {
        return "R ${readHandler.value} W ${writeHandler.value} E ${errorHandler.value}"
    }
}
