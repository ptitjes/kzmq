/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kzmq"

include(":kzmq-core")
include(":kzmq-test")
include(":kzmq-engine-cio")
include(":kzmq-engine-jeromq")
include(":kzmq-engine-libzmq")
include(":kzmq-engine-zeromqjs")
include(":kzmq-benchmarks")
include(":kzmq-tests")
include(":kzmq-tools")
