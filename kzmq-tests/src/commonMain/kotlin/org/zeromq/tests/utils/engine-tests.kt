/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.kotest.core.spec.style.scopes.*
import io.kotest.datatest.*
import org.zeromq.*

typealias EngineTest = suspend ContainerScope.(Pair<Context, Context>) -> Unit

fun <T : RootScope> T.withEngines(name: String, test: EngineTest) =
    withEngines(name).config(test = test)

fun <T : RootScope> T.withEngines(name: String): EngineTestBuilder =
    EngineTestBuilder(name, this)

class EngineTestBuilder(
    private val name: String,
    private val context: RootScope,
) {
    fun config(skipEngines: List<String> = listOf(), test: EngineTest) {
        context.runEngineTests(name, skipEngines, test)
    }
}

private fun RootScope.runEngineTests(
    name: String,
    skipEngines: List<String>,
    test: EngineTest,
) {
    val enginePairs = computeEnginePairs(skipEngines)

    withData(
        nameFn = { (engine1, engine2) -> "$name (${engine1.name}, ${engine2.name})" },
        enginePairs,
    ) { (engine1, engine2) ->
        test(Context(engine1) to Context(engine2))
    }
}

private fun computeEnginePairs(skipEngines: List<String>): List<Pair<Engine, Engine>> {
    val skipEnginesLowerCase = skipEngines.map { it.lowercase() }.toSet()

    val filteredEngines = engines.filter { engine ->
        val engineName = engine.name.lowercase()
        !skipEnginesLowerCase.any { it.contains(engineName) }
    }

    return filteredEngines.flatMap { engine1 -> filteredEngines.map { engine2 -> engine1 to engine2 } }
}
