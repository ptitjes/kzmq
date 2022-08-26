/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("UNUSED_VARIABLE")

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
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
        }

        val kotlinxCoroutinesVersion: String by project

        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
            }
        }
    }

    val nativeTargets = targets.withType<KotlinNativeTarget>()
    val windowsHostTargets = nativeTargets.filter { it.konanTarget.buildHost == Family.MINGW }
    val linuxHostTargets = nativeTargets.filter { it.konanTarget.buildHost == Family.LINUX }
    val osxHostTargets = nativeTargets.filter { it.konanTarget.buildHost == Family.OSX }
    val androidTargets = targets.withType<KotlinAndroidTarget>()
    val crossPlatformTargets = targets.filter { it !in nativeTargets }

    logger.info("Linux host targets: $linuxHostTargets")
    logger.info("OSX host targets: $osxHostTargets")
    logger.info("Windows host targets: $windowsHostTargets")
    logger.info("Android targets: $androidTargets")
    logger.info("Main host targets: $crossPlatformTargets")

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

    withType<CInteropProcess> { onlyIf { konanTarget.buildHost == HostManager.host.family } }

    withType<AbstractKotlinNativeCompile<*, *, *>> {
        onlyIf { compilation.konanTarget.buildHost == HostManager.host.family }
    }
}
