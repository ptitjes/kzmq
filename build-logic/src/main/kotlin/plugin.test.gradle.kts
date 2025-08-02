/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
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
                    implementation(libs.getLibrary("testBalloon.kotest.assertions"))
                }
            }

            all {
                languageSettings.optIn("io.kotest.common.ExperimentalKotest")
            }
        } else {
            commonTest {
                dependencies {
                    implementation(libs.getLibrary("testBalloon.kotest.assertions"))
                }
            }

            all {
                if (name.endsWith("Test")) {
                    languageSettings.optIn("io.kotest.common.ExperimentalKotest")
                }
            }
        }
    }
}

tasks.withType<AbstractTestTask> {
    failOnNoDiscoveredTests = false
}
