/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.*

plugins {
    kotlin("multiplatform")
}

val kotlinxCoroutinesVersion: String by project
val jeromqVersion: String by project

kotlin {
    explicitApi()
    optIns()

    nativeTargets()

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.apply {
            compilations["main"].cinterops {
                val libzmq by creating {
                    when (preset) {
                        presets["macosX64"] -> includeDirs.headerFilterOnly(
                            "/opt/local/include",
                            "/usr/local/include"
                        )

                        presets["linuxX64"] -> includeDirs.headerFilterOnly(
                            "/usr/include",
                            "/usr/include/x86_64-linux-gnu"
                        )

                        presets["mingwX64"] -> {
                            val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")
                            includeDirs.headerFilterOnly(mingwPath.resolve("include"))
                        }
                    }
                }
            }
        }
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
