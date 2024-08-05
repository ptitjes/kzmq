/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.library")
}

kotlin {
    jvmTargets()

    sourceSets {
        jvmMain {
            languageSettings {
                languageVersion = "2.1"
            }
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(libs.kotlinx.io.core)
                implementation(libs.jeromq)
            }
        }
    }
}
