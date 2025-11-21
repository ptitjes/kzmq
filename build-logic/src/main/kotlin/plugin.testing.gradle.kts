/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.common")
    id("de.infix.testBalloon")
}

kotlin {
    sourceSets {
        if (project.name.endsWith("-tests")) {
            commonMain {
                dependencies {
                    implementation(libs.getLibrary("testBalloon.framework.core"))
                    implementation(libs.getLibrary("testBalloon.assertions.kotest"))
                }
            }
        } else {
            commonTest {
                dependencies {
                    implementation(libs.getLibrary("testBalloon.framework.core"))
                    implementation(libs.getLibrary("testBalloon.assertions.kotest"))
                }
            }
        }
    }
}
