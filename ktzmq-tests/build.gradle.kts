/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

description = "Common tests for engines"

val kotlinxCoroutinesVersion: String by project
val junitVersion: String by project
val kotestVersion: String by project

plugins {
    id("io.kotest.multiplatform")
}

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        nodejs()
    }

    val hostOs = System.getProperty("os.name")

    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64()
        hostOs == "Linux" -> linuxX64()
        hostOs.startsWith("Windows") -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    hostTarget.apply {
        binaries.executable("test-publisher") {
            entryPoint = "temp.TestPublisher"
            freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
        }
        binaries.executable("test-subscriber") {
            entryPoint = "temp.TestSubscriber"
            freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
        }
        binaries.executable("test-throughput") {
            entryPoint = "temp.TestThroughput"
            freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                implementation("io.kotest:kotest-framework-datatest:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")

                implementation(project(":ktzmq-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":ktzmq-jeromq"))
                implementation(project(":ktzmq-cio"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":ktzmq-js"))
            }
        }
        val jsTest by getting {
        }

        val nativeMain by creating {
            findByName("commonMain")?.let { dependsOn(it) }

            dependencies {
                implementation(project(":ktzmq-libzmq"))
                implementation(project(":ktzmq-cio"))
            }
        }
        val nativeTest by creating {
            findByName("commonTest")?.let { dependsOn(it) }
        }

        hostTarget.let {
            getByName("${it.name}Main").dependsOn(nativeMain)
            getByName("${it.name}Test").dependsOn(nativeTest)
        }

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
        }
    }
}

tasks.getByName<Test>("jvmTest") {
    val javaToolchains = project.extensions.getByType<JavaToolchainService>()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(16))
    })
}
