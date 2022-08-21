/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.kotlin.dsl.*
import java.time.*

fun TaskContainerScope.setupTestTimeout() {
    withType<AbstractTestTask> {
        timeout.set(Duration.ofMinutes(10))
    }
}

fun TaskContainerScope.setupTestLogging() {
    withType<AbstractTestTask> {
        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
            showStackTraces = true
        }
        reports {
            junitXml.required.set(true)
        }
    }
}
