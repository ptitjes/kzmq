/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.library")
}

kotlin {
    jvmTargets()

    sourceSets {
        val jeromqVersion: String by project

        jvmMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation("org.zeromq:jeromq:$jeromqVersion")
            }
        }
    }
}
