package org.zeromq.tests.utils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.zeromq.Context
import org.zeromq.Engine

expect val engines: List<Engine>

expect val OS_NAME: String

fun contextTests(
    skipEngines: List<String> = emptyList(),
    block: suspend TestContextBuilder.() -> Unit
) {
    for (testedEngine in engines) {
        for (skipEngine in skipEngines) {
            val skipEngineArray = skipEngine.lowercase().split(":")

            val (platform, engine) = when (skipEngineArray.size) {
                2 -> skipEngineArray[0] to skipEngineArray[1]
                1 -> "*" to skipEngineArray[0]
                else -> throw IllegalStateException("Wrong skip engine format, expected 'engine' or 'platform:engine'")
            }

            val platformShouldBeSkipped = "*" == platform || OS_NAME == platform
            val engineShouldBeSkipped = "*" == engine || testedEngine.name.lowercase() == engine

            if (platformShouldBeSkipped && engineShouldBeSkipped) return
        }

        if (skipEngines.map { it.lowercase() }.contains(testedEngine.name.lowercase())) return

        testWithEngine(testedEngine, block)
    }
}

/**
 * Perform test with selected engine.
 */
@OptIn(DelicateCoroutinesApi::class)
fun testWithEngine(
    engine: Engine,
    block: suspend TestContextBuilder.() -> Unit
) = testSuspend {
    val builder = TestContextBuilder().apply { block() }

    val context = Context(engine)

    val job = launch {
        this.(builder.test)(context)
    }

    try {
        job.join()
    } catch (cause: Throwable) {
        job.cancel("Test failed", cause)
        throw cause
    } finally {
        builder.after(context)
    }
}
