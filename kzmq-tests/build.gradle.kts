/*
 * Copyright (c) 2021-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.*

description = "Common tests for engines"

plugins {
    id("plugin.common")
    id("plugin.atomicfu")
    id("plugin.kotest")
}

kotlin {
    jvmTargets()
    jsTargets()
    wasmJsTargets()
    nativeTargets { (it.isSupportedByCIO || it.isSupportedByLibzmq) && it.isSupportedByKtorNetwork }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(project(":kzmq-test"))
                implementation(libs.kotlinx.io.core)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.network)
            }
        }

        jsMain {
            dependencies {
                implementation(npm("find-open-port", libs.versions.findopenport.get()))
            }
        }

        wasmJsMain {
            dependencies {
                implementation(npm("find-open-port", libs.versions.findopenport.get()))
            }
        }

        nativeMain {
            dependencies {
                implementation(libs.ktor.network)
            }
        }

        jvmTest {
            dependencies {
                implementation(project(":kzmq-engine-jeromq"))
                implementation(project(":kzmq-engine-cio"))
            }
        }

        jsTest {
            dependencies {
                implementation(project(":kzmq-engine-zeromqjs"))
                implementation(project(":kzmq-engine-cio"))
            }
        }

        wasmJsTest {
            dependencies {
                implementation(project(":kzmq-engine-cio"))
            }
        }

        targets.withType<KotlinNativeTarget>().forEach { target ->
            getByName("${target.name}Test").apply {
                dependencies {
                    if (target.konanTarget.isSupportedByLibzmq) implementation(project(":kzmq-engine-libzmq"))
                    if (target.konanTarget.isSupportedByCIO) implementation(project(":kzmq-engine-cio"))
                }
            }
        }
    }
}

tasks.getByName<Test>("jvmTest") {
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}
