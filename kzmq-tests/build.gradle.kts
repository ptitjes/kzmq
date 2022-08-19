/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

description = "Common tests for engines"

plugins {
    kotlin("multiplatform")
    kotlin("plugin.atomicfu")
    id("io.kotest.multiplatform")
}

val kotlinxAtomicFuVersion: String by project
val kotlinxCoroutinesVersion: String by project
val junitVersion: String by project
val ktorVersion: String by project
val kotestVersion: String by project

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    optIns()

    jvmTargets()
    jsTargets()
    nativeTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                implementation("io.kotest:kotest-framework-datatest:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")

                implementation(project(":kzmq-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":kzmq-jeromq"))
                implementation(project(":kzmq-cio"))

                implementation("io.ktor:ktor-network:$ktorVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":kzmq-zeromqjs"))
                implementation(npm("find-open-port", "2.0.3"))
            }
        }
        val jsTest by getting

        val nativeMain by getting {
            dependencies {
                implementation(project(":kzmq-libzmq"))
                implementation(project(":kzmq-cio"))

                implementation("io.ktor:ktor-network:$ktorVersion")
            }
        }
        val nativeTest by getting
    }
}

tasks.getByName<Test>("jvmTest") {
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(16))
    })
}
