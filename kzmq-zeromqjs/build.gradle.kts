/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.library")
}

kotlin {
    jsTargets()

    sourceSets {
        val zeromqjsVersion: String by project

        jsMain {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(npm("zeromq", zeromqjsVersion))
            }
        }
    }
}
