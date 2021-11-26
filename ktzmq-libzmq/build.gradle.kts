/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

val kotlinxCoroutinesVersion: String by project
val jeromqVersion: String by project

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    val hostOs = System.getProperty("os.name")

    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    hostTarget.apply {
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
                    presets["mingwX64"] -> includeDirs.headerFilterOnly(mingwPath.resolve("include"))
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val nativeMain by getting {
            dependencies {
                implementation(project(":ktzmq-core"))
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
