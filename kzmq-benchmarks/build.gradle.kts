/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

description = "Common benchmarks for engines"

plugins {
    id("plugin.common")
    alias(libs.plugins.kotlinx.benchmark)
}

kotlin {
    jvmTargets()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(project(":kzmq-core"))
            }
        }

        jvmMain {
            dependencies {
                implementation(project(":kzmq-jeromq"))
                implementation(project(":kzmq-cio"))
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
    }
    configurations {
        val main by getting {
            warmups = 3
            iterations = 3
        }
    }
}
