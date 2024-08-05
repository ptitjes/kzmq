/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.library")
    id("plugin.atomicfu")
    id("plugin.kotest")
}

kotlin {
    jvmTargets()
    nativeTargets { it.isSupportedByCIO }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(libs.ktor.io)
                implementation(libs.ktor.network)
                implementation(libs.kermit)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":kzmq-test"))
            }
        }
    }
}
