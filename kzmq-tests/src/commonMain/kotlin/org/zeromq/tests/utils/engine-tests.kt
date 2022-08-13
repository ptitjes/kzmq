/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.kotest.core.spec.style.scopes.*
import io.kotest.datatest.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

typealias ContainedTest<T> = suspend ContainerScope.(T) -> Unit
typealias SingleContextTest = ContainedTest<Context>
typealias DualContextTest = ContainedTest<Pair<Context, Context>>

fun <T : RootScope> T.withContext(name: String, test: SingleContextTest) = withContext(name).config(test = test)

fun <T : RootScope> T.withContext(name: String): SingleContextTestBuilder = SingleContextTestBuilder(name, this)

fun <T : RootScope> T.withContexts(name: String, test: DualContextTest) = withContexts(name).config(test = test)

fun <T : RootScope> T.withContexts(name: String): DualContextTestBuilder = DualContextTestBuilder(name, this)

class SingleContextTestBuilder(
    private val name: String,
    private val context: RootScope,
) {
    fun config(
        skipEngines: List<String> = listOf(),
        onlyEngines: List<String>? = null,
        timeout: Duration? = null,
        test: SingleContextTest,
    ) {
        context.runSingleContextTest(name, skipEngines, onlyEngines, timeout, test)
    }
}

private val DEFAULT_TEST_TIMEOUT = 5.seconds

private fun RootScope.runSingleContextTest(
    name: String,
    skipEngines: List<String>,
    onlyEngines: List<String>?,
    timeout: Duration?,
    test: SingleContextTest,
) {
    val engines = computeEngines(skipEngines, onlyEngines)

    withData(
        nameFn = { engine -> "$name (${engine.name})" },
        engines,
    ) { engine ->
        Context(engine).use {
            withTimeout(timeout ?: DEFAULT_TEST_TIMEOUT) {
                test(it)
            }
        }
    }
}

private fun computeEngines(
    skipEngines: List<String>,
    onlyEngines: List<String>?,
): List<EngineFactory> {
    val skipEnginesLowerCase = skipEngines.map { it.lowercase() }.toSet()
    val onlyEnginesLowerCase = onlyEngines?.map { e1 -> e1.lowercase() }

    val engines = enginesUnderTest.filter { engine ->
        val engineName = engine.name.lowercase()
        !skipEnginesLowerCase.any { it.contains(engineName) }
    }

    return engines.filter { e ->
        onlyEnginesLowerCase?.any { oe ->
            oe.contains(e.name)
        } ?: true
    }
}

class DualContextTestBuilder(
    private val name: String,
    private val context: RootScope,
) {
    fun config(
        skipEngines: List<String> = listOf(),
        skipEnginePairs: List<Pair<String, String>>? = null,
        onlyEnginePairs: List<Pair<String, String>>? = null,
        timeout: Duration? = null,
        test: DualContextTest,
    ) {
        context.runDualContextTest(name, skipEngines, skipEnginePairs, onlyEnginePairs, timeout, test)
    }
}

private fun RootScope.runDualContextTest(
    name: String,
    skipEngines: List<String>,
    skipEnginePairs: List<Pair<String, String>>?,
    onlyEnginePairs: List<Pair<String, String>>?,
    timeout: Duration?,
    test: DualContextTest,
) {
    val enginePairs = computeEnginePairs(skipEngines, skipEnginePairs, onlyEnginePairs)

    withData(
        nameFn = { (engine1, engine2) -> "$name (${engine1.name}, ${engine2.name})" },
        enginePairs,
    ) { (engine1, engine2) ->
        withTimeout(timeout ?: DEFAULT_TEST_TIMEOUT) {
            (Context(engine1) to Context(engine2)).use {
                test(it)
            }
        }
    }
}

private fun computeEnginePairs(
    skipEngines: List<String>,
    skipEnginePairs: List<Pair<String, String>>?,
    onlyEnginePairs: List<Pair<String, String>>?,
): List<Pair<EngineFactory, EngineFactory>> {
    val skipEnginesLowerCase = skipEngines.map { it.lowercase() }.toSet()
    val skipEnginePairsLowerCase = skipEnginePairs?.map { (e1, e2) -> e1.lowercase() to e2.lowercase() }
    val onlyEnginePairsLowerCase = onlyEnginePairs?.map { (e1, e2) -> e1.lowercase() to e2.lowercase() }

    val filteredEngines = enginesUnderTest.filter { engine ->
        val engineName = engine.name.lowercase()
        !skipEnginesLowerCase.any { it.contains(engineName) }
    }

    val enginePairs = filteredEngines.flatMap { engine1 ->
        filteredEngines.map { engine2 -> engine1 to engine2 }
    }

    return enginePairs.filter { (e1, e2) ->
        (onlyEnginePairsLowerCase?.any { (oe1, oe2) ->
            oe1.contains(e1.name) && oe2.contains(e2.name)
        } ?: true) && !(skipEnginePairsLowerCase?.any { (oe1, oe2) ->
            oe1.contains(e1.name) && oe2.contains(e2.name)
        } ?: false)
    }
}
