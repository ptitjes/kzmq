/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    val kotlinVersion: String by settings
    val dokkaVersion: String by settings
    val kotlinxKoverVersion: String by settings
    val kotestPluginVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.atomicfu") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("org.jetbrains.kotlinx.kover") version kotlinxKoverVersion
        id("io.kotest.multiplatform") version kotestPluginVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kzmq"

include(":kzmq-core")
include(":kzmq-zeromqjs")
include(":kzmq-jeromq")
include(":kzmq-libzmq")
include(":kzmq-cio")
include(":kzmq-tests")
include(":kzmq-tools")
