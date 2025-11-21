/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.library")
    id("plugin.atomicfu")
    id("plugin.testing")
    id("plugin.mocks")
}

kotlin {
    jvmTargets()
    jsTargets()
    nativeTargets { it.isSupportedByCIO || it.isSupportedByLibzmq }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.io.core)
                implementation(project(":kzmq-core"))

                implementation(libs.testBalloon.framework.core)
                implementation(libs.testBalloon.assertions.kotest)
            }
        }
    }
}
