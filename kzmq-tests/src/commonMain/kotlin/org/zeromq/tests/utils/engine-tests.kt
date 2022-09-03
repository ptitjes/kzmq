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
        skip: Set<String>? = null,
        only: Set<String>? = null,
        timeout: Duration? = null,
        test: SingleContextTest,
    ) {
        context.runSingleContextTest(name, skip, only, timeout, test)
    }
}

private val DEFAULT_TEST_TIMEOUT = 5.seconds

private fun RootScope.runSingleContextTest(
    name: String,
    skip: Set<String>?,
    only: Set<String>?,
    timeout: Duration?,
    test: SingleContextTest,
) {
    val engines = computeEngines(skip, only)

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
    skip: Set<String>?,
    only: Set<String>?,
): List<EngineFactory> {
    return engines.filterContainingLower(skip, only) { it.name.lowercase() }
}

class DualContextTestBuilder(
    private val name: String,
    private val context: RootScope,
) {
    fun config(
        skip: Set<String>? = null,
        only: Set<String>? = null,
        timeout: Duration? = null,
        test: DualContextTest,
    ) {
        context.runDualContextTest(name, skip, only, timeout, test)
    }
}

private fun RootScope.runDualContextTest(
    name: String,
    skip: Set<String>? = null,
    only: Set<String>? = null,
    timeout: Duration?,
    test: DualContextTest,
) {
    val enginePairs = computeEnginePairs(skip, only)

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
    skip: Set<String>?,
    only: Set<String>?,
): List<Pair<EngineFactory, EngineFactory>> {
    val enginePairs = engines.flatMap { e1 -> engines.map { e2 -> e1 to e2 } }
    return enginePairs.filterContainingLower(skip, only) { (e1, e2) -> "${e1.name.lowercase()}-${e2.name.lowercase()}" }
}

fun <T> List<T>.filterContainingLower(
    skip: Set<String>? = null,
    only: Set<String>? = null,
    labeller: (T) -> String,
): List<T> {
    return filter { element ->
        val label = labeller(element)
        skip?.none { label.contains(it.lowercase()) } ?: true && only?.any { label.contains(it.lowercase()) } ?: true
    }
}
