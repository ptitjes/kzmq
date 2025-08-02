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
                    kotlin("test")
                    implementation(libs.getLibrary("testBalloonFramework"))
                }
            }
        } else {
            commonTest {
                dependencies {
                    kotlin("test")
                    implementation(libs.getLibrary("testBalloonFramework"))
                }
            }
        }
    }
}

tasks.withType<AbstractTestTask> {
    failOnNoDiscoveredTests = false
}
