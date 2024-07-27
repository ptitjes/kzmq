/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.target.Architecture

plugins {
    id("plugin.library")
}

kotlin {
    nativeTargets { it.isSupportedByLibzmq }

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.apply {
            compilations["main"].cinterops {
                create("libzmq") {
                    when (konanTarget.family) {
                        Family.LINUX -> when (konanTarget.architecture) {
                            Architecture.X64 -> includeDirs.allHeaders("/usr/include", "/usr/include/x86_64-linux-gnu")
                            Architecture.ARM64 -> includeDirs.allHeaders("/usr/include", "/usr/include/arm64-linux-gnu")
                            else -> error("Unknown Linux architecture '${konanTarget.architecture}'")
                        }

                        Family.OSX -> includeDirs.allHeaders("/opt/local/include", "/usr/local/include")

                        Family.MINGW -> {
                            val mingwPath = File(
                                when (konanTarget.architecture) {
                                    Architecture.X64 -> System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64"
                                    else -> error("Unknown Mingw architecture")
                                }
                            )
                            includeDirs.allHeaders(mingwPath.resolve("include"))
                        }

                        else -> {}
                    }
                    compilerOpts += pkgConfig("--cflags", "libzmq")
                }
            }
        }
    }

    sourceSets {
        nativeMain {
            dependencies {
                implementation(project(":kzmq-core"))
            }
        }
    }
}
