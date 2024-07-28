/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.library")
}

kotlin {
    jsTargets()

    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(npm("zeromq", libs.versions.zeromqjs.get()))
            }
        }
    }
}
