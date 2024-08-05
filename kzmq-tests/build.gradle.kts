/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
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

        nativeMain {
            dependencies {
                implementation(libs.ktor.network)
            }
        }

        jvmTest {
            dependencies {
                implementation(project(":kzmq-jeromq"))
                implementation(project(":kzmq-cio"))
            }
        }

        jsTest {
            dependencies {
                implementation(project(":kzmq-zeromqjs"))
            }
        }

        targets.withType<KotlinNativeTarget>().forEach { target ->
            getByName("${target.name}Test").apply {
                dependencies {
                    if (target.konanTarget.isSupportedByLibzmq) implementation(project(":kzmq-libzmq"))
                    if (target.konanTarget.isSupportedByCIO) implementation(project(":kzmq-cio"))
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
