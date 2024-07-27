/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.common")
    id("io.kotest.multiplatform")
}

kotlin {
    sourceSets {
        if (project.name.endsWith("-tests")) {
            commonMain {
                dependencies {
                    implementation(libs.getLibrary("kotest.framework.engine"))
                    implementation(libs.getLibrary("kotest.framework.datatest"))
                    implementation(libs.getLibrary("kotest.assertions.core"))
                }
            }

            all {
                languageSettings.optIn("io.kotest.common.ExperimentalKotest")
            }
        } else {
            commonTest {
                dependencies {
                    implementation(libs.getLibrary("kotest.framework.engine"))
                    implementation(libs.getLibrary("kotest.framework.datatest"))
                    implementation(libs.getLibrary("kotest.assertions.core"))
                }
            }

            all {
                if (name.endsWith("Test")) {
                    languageSettings.optIn("io.kotest.common.ExperimentalKotest")
                }
            }
        }

        val hasJvmSupport = project.projectDir.resolve("src/jvmMain").exists()
        if (hasJvmSupport) {
            create("jvmTest").apply {
                dependencies {
                    implementation(libs.getLibrary("kotest.runner.junit5"))
                }
            }
        }
    }
}
