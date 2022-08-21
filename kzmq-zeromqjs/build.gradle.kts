/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

val kotlinxCoroutinesVersion: String by project
val jeromqVersion: String by project

kotlin {
    explicitApi()
    optIns()

    jsTargets()

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation(npm("zeromq", "6.0.0-beta.6"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
            }
        }
    }
}
