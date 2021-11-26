package org.zeromq.tests.utils

import io.kotest.core.*
import io.kotest.core.names.*
import io.kotest.core.spec.style.scopes.*
import io.kotest.core.test.*
import io.kotest.core.test.config.*
import io.kotest.datatest.*
import org.zeromq.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

typealias EngineTest = suspend ContainerScope.(Pair<Context, Context>) -> Unit

fun <T : RootScope> T.withEngines(name: String, test: EngineTest) =
    withEngines(name).config(test = test)

fun <T : RootScope> T.withEngines(name: String): EngineTestWithConfigBuilder<T> =
    EngineTestWithConfigBuilder<T>(name, false, this)

class EngineTestWithConfigBuilder<T>(
    private val name: String,
    private val disabled: Boolean,
    private val context: RootScope,
) {
    fun config(
        skipEngines: List<String> = listOf(),
        enabled: Boolean? = null,
        enabledIf: EnabledIf? = null,
        enabledOrReasonIf: EnabledOrReasonIf? = null,
        tags: Set<Tag>? = null,
        timeout: Duration? = 10.seconds,
        failfast: Boolean? = null,
        test: EngineTest,
    ) {
        val config = UnresolvedTestConfig(
            enabled = enabled,
            enabledIf = enabledIf,
            enabledOrReasonIf = enabledOrReasonIf,
            tags = tags,
            timeout = timeout,
            failfast = failfast,
        )

        context.addContainer(TestName("context", name, false), disabled, config) {
            runEngineTests(skipEngines, test)
        }
    }
}

private suspend fun ContainerScope.runEngineTests(skipEngines: List<String>, test: EngineTest) {
    val enginePairs = computeEnginePairs(skipEngines)

    withData(
        nameFn = { (engine1, engine2) -> "(${engine1.name}, ${engine2.name})" },
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
