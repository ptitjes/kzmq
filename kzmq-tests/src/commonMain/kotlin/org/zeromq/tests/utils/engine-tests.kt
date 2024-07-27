/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import io.kotest.core.names.*
import io.kotest.core.spec.style.scopes.*
import io.kotest.core.test.*
import io.kotest.core.test.config.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

typealias SingleContextTest = suspend ContainerScope.(Context, Protocol) -> Unit
typealias DualContextTest = suspend ContainerScope.(Context, Context, Protocol) -> Unit

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
    val testData = engines.flatMap { engine -> engine.supportedTransports.map { engine to it.asProtocol() } }

    val enableTest = enableTest<Pair<EngineFactory, Protocol>>(skip, only) { (engine, protocol) ->
        "${engine.name.lowercase()}, ${protocol.name.lowercase()}"
    }

    val testTimeout = timeout ?: DEFAULT_TEST_TIMEOUT
    val globalConfig = UnresolvedTestConfig(timeout = testTimeout * 2)

    testData.forEach { data ->
        val (engine, protocol) = data
        val testName = TestName("$name (${engine.name}, $protocol)")
        val testConfig = globalConfig.copy(enabled = enableTest(data))

        addTest(testName, false, testConfig, TestType.Dynamic) {
            val context = Context(engine)
            context.use {
                withTimeout(testTimeout) {
                    test(context, protocol)
                }
            }
        }
    }
}

private fun String.asProtocol(): Protocol = Protocol.valueOf(uppercase())

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
    val enginePairs = engines.flatMap { e1 -> engines.map { e2 -> e1 to e2 } }
    val testData = enginePairs.flatMap { (e1, e2) ->
        (e1.supportedTransports intersect e2.supportedTransports).map { Triple(e1, e2, it.asProtocol()) }
            .filter { (e1, e2, p) -> p != Protocol.INPROC || e1 == e2 }
    }

    val enableTest = enableTest<Triple<EngineFactory, EngineFactory, Protocol>>(skip, only) { (e1, e2, protocol) ->
        "${e1.name.lowercase()}-${e2.name.lowercase()}, ${protocol.name.lowercase()}"
    }

    val testTimeout = timeout ?: DEFAULT_TEST_TIMEOUT
    val globalConfig = UnresolvedTestConfig(timeout = testTimeout * 2)

    testData.forEach { data ->
        val (engine1, engine2, protocol) = data
        val testName = TestName("$name (${engine1.name}-${engine2.name}, $protocol)")
        val testConfig = globalConfig.copy(enabled = enableTest(data))

        addTest(testName, false, testConfig, TestType.Dynamic) {
            val context1 = Context(engine1)
            val context2 = if (protocol == Protocol.INPROC) context1 else Context(engine2)
            use(context1, context2) {
                withTimeout(testTimeout) {
                    test(context1, context2, protocol)
                }
            }
        }
    }
}

private fun <T> enableTest(
    skip: Set<String>?,
    only: Set<String>?,
    labeller: (T) -> String,
): (element: T) -> Boolean = { element ->
    val label = labeller(element)
    skip?.none { label.contains(it.lowercase()) } ?: true && only?.any { label.contains(it.lowercase()) } ?: true
}
