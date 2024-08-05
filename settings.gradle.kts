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
include(":kzmq-zeromqjs")
include(":kzmq-jeromq")
include(":kzmq-libzmq")
include(":kzmq-cio")
include(":kzmq-benchmarks")
include(":kzmq-tests")
include(":kzmq-tools")
