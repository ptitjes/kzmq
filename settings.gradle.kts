/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    }
}

rootProject.name = "kzmq"

include(":kzmq-core")
include(":kzmq-js")
include(":kzmq-jeromq")
include(":kzmq-libzmq")
include(":kzmq-cio")
include(":kzmq-tests")
include(":kzmq-tools")
