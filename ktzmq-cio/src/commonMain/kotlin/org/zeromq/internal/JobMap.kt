package org.zeromq.internal

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
