/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.tasks.testing.logging.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.konan.target.*
import java.time.*

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    jvmToolchain(17)

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
        }

        commonMain {
            dependencies {
                implementation(libs.getLibrary("kotlinx.coroutines.core"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.getLibrary("kotlinx.coroutines.test"))
            }
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()

        timeout.set(Duration.ofMinutes(10))

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

    withType<CInteropProcess> {
        onlyIf {
            konanTarget.buildHost == HostManager.host.family
                && konanTarget.architecture == HostManager.host.architecture
        }
    }

    withType<KotlinNativeCompile> {
        onlyIf {
            this.target !in listOf("linux_arm64", "mingw_x64")
        }
    }
}

kotlin.targets.withType<KotlinNativeTarget> {
    // Do not activate backtrace for Mingw
    // https://kotlinlang.org/docs/whatsnew1620.html?_ga=2.5870007.58710271.1649248900-2086887657.1620731764#better-stack-traces-with-libbacktrace
    // https://youtrack.jetbrains.com/issue/KT-51866/Compile-error-to-mingwX64-with-libbacktrace
    if (this.konanTarget.family != Family.MINGW) {
        binaries.all {
            binaryOptions["sourceInfoType"] = "libbacktrace"
        }
    }
}
