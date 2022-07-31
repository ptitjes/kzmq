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

typealias EngineTest = suspend ContainerScope.(Pair<Context, Context>) -> Unit

fun <T : RootScope> T.withEngines(name: String, test: EngineTest) =
    withEngines(name).config(test = test)

fun <T : RootScope> T.withEngines(name: String): EngineTestBuilder =
    EngineTestBuilder(name, this)

class EngineTestBuilder(
    private val name: String,
    private val context: RootScope,
) {
    fun config(
        skipEngines: List<String> = listOf(),
        onlyEnginePairs: List<Pair<String, String>>? = null,
        timeout: Duration? = null,
        test: EngineTest,
    ) {
        context.runEngineTests(name, skipEngines, onlyEnginePairs, timeout, test)
    }
}

private fun RootScope.runEngineTests(
    name: String,
    skipEngines: List<String>,
    onlyEnginePairs: List<Pair<String, String>>?,
    timeout: Duration?,
    test: EngineTest,
) {
    val enginePairs = computeEnginePairs(skipEngines, onlyEnginePairs)

    withData(
        nameFn = { (engine1, engine2) -> "$name (${engine1.name}, ${engine2.name})" },
        enginePairs,
    ) { (engine1, engine2) ->
        if (timeout != null) {
            withTimeout(timeout) {
                test(Context(engine1) to Context(engine2))
            }
        } else {
            test(Context(engine1) to Context(engine2))
        }
    }
}

private fun computeEnginePairs(
    skipEngines: List<String>,
    onlyEnginePairs: List<Pair<String, String>>?,
): List<Pair<Engine, Engine>> {
    val skipEnginesLowerCase = skipEngines.map { it.lowercase() }.toSet()
    val onlyEnginePairsLowerCase = onlyEnginePairs?.map { (e1, e2) -> e1.lowercase() to e2.lowercase() }

    val filteredEngines = engines.filter { engine ->
        val engineName = engine.name.lowercase()
        !skipEnginesLowerCase.any { it.contains(engineName) }
    }

    val enginePairs = filteredEngines.flatMap { engine1 ->
        filteredEngines.map { engine2 -> engine1 to engine2 }
    }

    return enginePairs.filter { (e1, e2) ->
        onlyEnginePairsLowerCase?.any { (oe1, oe2) ->
            oe1.contains(e1.name) && oe2.contains(e2.name)
        } ?: true
    }
}
