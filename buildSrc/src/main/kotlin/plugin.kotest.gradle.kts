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
        val kotestVersion: String by project

        if (project.name.endsWith("-tests")) {
            commonMain {
                dependencies {
                    implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                    implementation("io.kotest:kotest-framework-datatest:$kotestVersion")
                    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                }
            }

            all {
                languageSettings.optIn("io.kotest.common.ExperimentalKotest")
            }
        } else {
            commonTest {
                dependencies {
                    implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                    implementation("io.kotest:kotest-framework-datatest:$kotestVersion")
                    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
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
                    implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                }
            }
        }
    }
}
