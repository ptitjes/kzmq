package org.zeromq.tests.utils

import kotlinx.coroutines.*
import org.zeromq.*

expect val engines: List<Engine>

expect val OS_NAME: String

fun contextTests(
    skipEngines: List<String> = emptyList(),
    block: suspend TestContextBuilder.() -> Unit,
) {
    val skipEnginesLowerCase = skipEngines.map { it.lowercase() }.toSet()

    val filteredEngines = engines.filter { engine ->
        val name = engine.name.lowercase()
        !skipEnginesLowerCase.any { it.contains(name) }
    }

    val enginePairs = filteredEngines.flatMap { engine1 -> filteredEngines.map { engine2 -> engine1 to engine2 } }

    val failures = mutableListOf<TestFailure>()
    for (engines in enginePairs) {
        val result = runCatching {
            testWithEngines(engines, block)
        }

        if (result.isFailure) {
            failures += TestFailure(engines.toString(), result.exceptionOrNull()!!)
        }
    }

    if (failures.isEmpty()) return
    error(failures.joinToString("\n"))
}

private class TestFailure(val name: String, val cause: Throwable) {
    override fun toString(): String = buildString {
        appendLine("Test failed with engines: $name")
        appendLine(cause)
        for (stackLine in cause.stackTraceToString().lines()) {
            appendLine("\t$stackLine")
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun testWithEngines(
    engines: Pair<Engine, Engine>,
    block: suspend TestContextBuilder.() -> Unit,
) = testSuspend {
    val builder = TestContextBuilder().apply { block() }

    withTimeout(builder.timeoutSeconds * 1000L) {
        val (engine1, engine2) = engines
        val contexts = Context(engine1) to Context(engine2)

        val job = launch(start = CoroutineStart.LAZY) {
            this.(builder.test)(contexts)
        }

        try {
            job.join()
        } catch (cause: Throwable) {
            job.cancel("Test failed", cause)
            throw cause
        } finally {
            builder.after(contexts)

            val (ctx1, ctx2) = contexts
            ctx1.close()
            ctx2.close()
        }
    }
}
