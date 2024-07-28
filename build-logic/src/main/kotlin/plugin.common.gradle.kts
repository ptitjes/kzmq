/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.tasks.testing.logging.*
import org.gradle.kotlin.dsl.*
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

    val nativeTargets = targets.withType<KotlinNativeTarget>()
    val windowsHostTargets = nativeTargets.filter { it.konanTarget.buildHost == Family.MINGW }
    val linuxHostTargets = nativeTargets.filter { it.konanTarget.buildHost == Family.LINUX }
    val osxHostTargets = nativeTargets.filter { it.konanTarget.buildHost == Family.OSX }
    val androidTargets = targets.withType<KotlinAndroidTarget>()
    val crossPlatformTargets = targets.filter { it !in nativeTargets }

    linuxHostTargets.onlyBuildIf { !CI || HostManager.hostIsLinux }
    osxHostTargets.onlyBuildIf { !CI || HostManager.hostIsMac }
    windowsHostTargets.onlyBuildIf { !CI || HostManager.hostIsMingw }
    crossPlatformTargets.onlyBuildIf { !CI || SANDBOX || isMainHost }
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
