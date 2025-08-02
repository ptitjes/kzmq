/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import de.infix.testBalloon.framework.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes

typealias SingleContextTest = suspend TestCoroutineScope.(Context, Protocol) -> Unit
typealias DualContextTest = suspend TestCoroutineScope.(Context, Context, Protocol) -> Unit

fun TestSuite.withContext(name: String, testAction: SingleContextTest) = withContext(name).config(test = testAction)

fun TestSuite.withContext(name: String): SingleContextTestBuilder = SingleContextTestBuilder(name, this)

fun TestSuite.withContexts(name: String, testAction: DualContextTest) = withContexts(name).config(test = testAction)

fun TestSuite.withContexts(name: String): DualContextTestBuilder = DualContextTestBuilder(name, this)

class SingleContextTestBuilder(
    private val name: String,
    private val context: TestSuite,
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

private val DEFAULT_TEST_TIMEOUT = 1.minutes

private fun TestSuite.runSingleContextTest(
    name: String,
    skip: Set<String>?,
    only: Set<String>?,
    timeout: Duration?,
    testAction: SingleContextTest,
) {
    val testData = engines.flatMap { engine -> engine.supportedTransports.map { engine to it.asProtocol() } }

    val enableTest = enableTest<Pair<EngineFactory, Protocol>>(skip, only) { (engine, protocol) ->
        "${engine.name.lowercase()}, ${protocol.name.lowercase()}"
    }

    val testTimeout = timeout ?: DEFAULT_TEST_TIMEOUT
    val config = TestConfig.testScope(isEnabled = false, timeout = testTimeout)

    testSuite(name) {
        testData.forEach { data ->
            val (engine, protocol) = data

            val testName = "${engine.name}, $protocol"
            val testConfig = if (enableTest(data)) config else config.disable()

            test(testName, testConfig) {
                val context = Context(engine)
                context.use {
                    withTimeout(testTimeout) {
                        testAction(context, protocol)
                    }
                }
            }
        }
    }
}

private fun String.asProtocol(): Protocol = Protocol.valueOf(uppercase())

class DualContextTestBuilder(
    private val name: String,
    private val context: TestSuite,
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

private fun TestSuite.runDualContextTest(
    name: String,
    skip: Set<String>? = null,
    only: Set<String>? = null,
    timeout: Duration?,
    testAction: DualContextTest,
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
    val config = TestConfig.testScope(isEnabled = false, timeout = testTimeout)

    testSuite(name) {
        testData.forEach { data ->
            val (engine1, engine2, protocol) = data
            val testName = "${engine1.name}-${engine2.name}, $protocol"
            val testConfig = if (enableTest(data)) config else config.disable()

            test(testName, testConfig) {
                val context1 = Context(engine1)
                val context2 = if (protocol == Protocol.INPROC) context1 else Context(engine2)
                use(context1, context2) {
                    withTimeout(testTimeout) {
                        testAction(context1, context2, protocol)
                    }
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
