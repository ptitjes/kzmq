/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform")
    kotlin("plugin.atomicfu")
}

val kotlinxAtomicFuVersion: String by project
val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project
val kermitVersion: String by project

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val hostOs = System.getProperty("os.name")

    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64()
        hostOs == "Linux" -> linuxX64()
        hostOs.startsWith("Windows") -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("io.ktor:ktor-io:$ktorVersion")
                implementation("io.ktor:ktor-network:$ktorVersion")
                implementation("co.touchlab:kermit:$kermitVersion")
                implementation(project(":kzmq-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val nativeMain by creating {
            findByName("commonMain")?.let { dependsOn(it) }
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
