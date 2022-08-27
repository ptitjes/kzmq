/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
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
        val ktorVersion: String by project
        val kermitVersion: String by project

        commonMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation("io.ktor:ktor-io:$ktorVersion")
                implementation("io.ktor:ktor-network:$ktorVersion")
                implementation("co.touchlab:kermit:$kermitVersion")
            }
        }
    }
}
