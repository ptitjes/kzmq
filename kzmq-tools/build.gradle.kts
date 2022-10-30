/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.target.Architecture

plugins {
    id("plugin.common")
    application
}

kotlin {
    jvmToolchain(17)

    jvmTargets()
    nativeTargets { it.family == Family.LINUX && it.architecture == Architecture.X64 }

    jvm {
        withJava()
    }

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.apply {
            binaries {
                executable("throughput") { entryPoint = "org.zeromq.tools.main" }
            }
            compilations["main"].enableEndorsedLibs = false
        }
    }

    sourceSets {
        val kotlinxCliVersion: String by project

        commonMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(project(":kzmq-cio"))
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
            }
        }

        jvmMain {
            dependencies {
                implementation(project(":kzmq-jeromq"))
            }
        }
    }
}

application {
    mainClass.set("org.zeromq.tools.ThroughputKt")
}
