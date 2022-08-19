/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform")
}

val kotlinxCoroutinesVersion: String by project
val kotlinxAtomicFuVersion: String by project
val jeromqVersion: String by project

kotlin {
    explicitApi()
    optIns()

    jvmTargets()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("org.zeromq:jeromq:$jeromqVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
