/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import de.infix.testBalloon.framework.core.*
import io.kotest.assertions.*
import kotlinx.coroutines.*
import org.zeromq.*
import kotlin.jvm.*
import kotlin.time.Duration.Companion.minutes

interface ContextTestConfigScope {
    fun skip(vararg skipped: String)
    fun skipAll()
}

fun TestSuite.singleContextTest(
    name: String,
    config: ContextTestConfigScope.() -> Unit = {},
    test: suspend TestExecutionScope.(Context, Protocol) -> Unit,
) {
    val testCases = engines.flatMap { engine ->
        engine.supportedTransports.map { SingleContextTestCase(engine, it.asProtocol()) }
    }.toSet()

    val skippedTestCases = testCases.computeSkippedTestCases(config)

    val testTimeout = DEFAULT_TEST_TIMEOUT
    val globalConfig = TestConfig.testScope(isEnabled = false, timeout = testTimeout)

    testSuite(name = name, testConfig = globalConfig) {
        testCases.forEach { testCase ->
            val (engine, protocol) = testCase

            val testName = "${engine.name}, $protocol"
            val disabled = testCase in skippedTestCases
            val testConfig = if (disabled) globalConfig.disable() else globalConfig

            test(testName, testConfig = testConfig) {
                retry(10, 5.minutes) {
                    val context = Context(engine)
                    context.use {
                        withTimeout(testTimeout) {
                            test(context, protocol)
                        }
                    }
                }
            }
        }
    }
}

fun TestSuite.dualContextTest(
    name: String,
    config: ContextTestConfigScope.() -> Unit = {},
    test: suspend TestExecutionScope.(Context, Context, Protocol) -> Unit,
) {
    val testCases = engines.flatMap { e1 -> engines.map { e2 -> e1 to e2 } }.flatMap { (e1, e2) ->
        (e1.supportedTransports intersect e2.supportedTransports)
            .map { DualContextTestCase(e1, e2, it.asProtocol()) }
            .filter { (e1, e2, p) -> p != Protocol.INPROC || e1 == e2 }
    }.toSet()

    val skippedTestCases = testCases.computeSkippedTestCases(config)

    val testTimeout = DEFAULT_TEST_TIMEOUT
    val globalConfig = TestConfig.testScope(isEnabled = false, timeout = testTimeout)

    testSuite(name = name, testConfig = globalConfig) {
        testCases.forEach { testCase ->
            val (engine1, engine2, protocol) = testCase

            val testName = "${engine1.name}-${engine2.name}, $protocol"
            val disabled = testCase in skippedTestCases
            val testConfig = if (disabled) globalConfig.disable() else globalConfig

            test(testName, testConfig = testConfig) {
                retry(10, 5.minutes) {
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
    }
}

private val DEFAULT_TEST_TIMEOUT = 5.minutes

private data class SingleContextTestCase(
    val engine: EngineFactory,
    val protocol: Protocol,
) {
    override fun toString(): String = "${engine.name.lowercase()}, ${protocol.name.lowercase()}"
}

private data class DualContextTestCase(
    val engine1: EngineFactory,
    val engine2: EngineFactory,
    val protocol: Protocol,
) {
    override fun toString(): String =
        "${engine1.name.lowercase()}-${engine2.name.lowercase()}, ${protocol.name.lowercase()}"
}

@JvmName("computeSkippedSingleTestCases")
private fun Set<SingleContextTestCase>.computeSkippedTestCases(
    config: ContextTestConfigScope.() -> Unit
): Set<SingleContextTestCase> {
    val scope = SingleContextTestConfigScope(this)
    scope.config()
    val skippedTestCases = scope.skippedTestCases
    return skippedTestCases
}

private class SingleContextTestConfigScope(
    val testCases: Set<SingleContextTestCase>,
) : ContextTestConfigScope {
    private val _skippedTestCases = mutableSetOf<SingleContextTestCase>()
    val skippedTestCases: Set<SingleContextTestCase> get() = _skippedTestCases.toSet()

    override fun skip(vararg skipped: String) {
        skipped.forEach { skipped ->
            _skippedTestCases += testCases.filter { testCase ->
                skipped.lowercase() in testCase.toString()
            }
        }
    }

    override fun skipAll() {
        _skippedTestCases += testCases
    }
}

@JvmName("computeSkippedDualTestCases")
private fun Set<DualContextTestCase>.computeSkippedTestCases(
    config: ContextTestConfigScope.() -> Unit
): Set<DualContextTestCase> {
    val scope = DualContextTestConfigScope(this)
    scope.config()
    val skippedTestCases = scope.skippedTestCases
    return skippedTestCases
}

private class DualContextTestConfigScope(
    val testCases: Set<DualContextTestCase>,
) : ContextTestConfigScope {
    private val _skippedTestCases = mutableSetOf<DualContextTestCase>()
    val skippedTestCases: Set<DualContextTestCase> get() = _skippedTestCases.toSet()

    override fun skip(vararg skipped: String) {
        skipped.forEach { skipped ->
            _skippedTestCases += testCases.filter { testCase ->
                skipped.lowercase() in testCase.toString()
            }
        }
    }

    override fun skipAll() {
        _skippedTestCases += testCases
    }
}

private fun String.asProtocol(): Protocol = Protocol.valueOf(uppercase())
