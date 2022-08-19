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
    optIns()

    jvmTargets()
    nativeTargets()

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
    }
}
