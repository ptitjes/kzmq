/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import kotlinx.atomicfu.*
import kotlinx.coroutines.*

internal class JobMap<K> {
    private val jobs = atomic(mapOf<K, JobContainer>())

    fun add(key: K, factory: () -> Job) {
        val container = JobContainer(factory)
        val putContainer = jobs.updateAndGet { it + (key to container) }[key]
        if (container == putContainer) {
            container.start()
            container.invokeOnCompletion { cause ->
                if (cause == null || cause !is CancellationException) jobs.update { it - key }
            }
        }
    }

    fun remove(key: K) {
        jobs.getAndUpdate { it - key }[key]?.cancel()
    }

    fun removeAll() {
        val jobs = jobs.getAndSet(mapOf())
        jobs.forEach { (_, container) -> container.cancel() }
    }
}

private class JobContainer(private val factory: () -> Job) {
    private lateinit var job: Job

    fun start() {
        job = factory()
    }

    fun cancel() {
        job.cancel()
    }

    fun invokeOnCompletion(handler: CompletionHandler) {
        job.invokeOnCompletion(handler)
    }
}
