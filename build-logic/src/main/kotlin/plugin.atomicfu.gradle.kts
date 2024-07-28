/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.common")
    kotlin("plugin.atomicfu")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.getLibrary("kotlinx.atomicfu"))
            }
        }
    }
}
