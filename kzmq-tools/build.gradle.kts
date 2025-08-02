/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.target.Architecture

plugins {
    id("plugin.common")
}

kotlin {
    jvmToolchain(17)

    jvmTargets()
    nativeTargets { it.family == Family.LINUX && it.architecture == Architecture.X64 }

    jvm {
        binaries {
            executable {
                mainClass = "org.zeromq.tools.ThroughputKt"
            }
        }
    }

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.apply {
            binaries {
                executable("throughput") { entryPoint = "org.zeromq.tools.main" }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(project(":kzmq-engine-cio"))
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.cli)
            }
        }

        jvmMain {
            dependencies {
                implementation(project(":kzmq-engine-jeromq"))
            }
        }
    }
}
