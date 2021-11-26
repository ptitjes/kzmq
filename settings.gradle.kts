/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
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

rootProject.name = "ktzmq"

include(":ktzmq-core")
include(":ktzmq-js")
include(":ktzmq-jeromq")
include(":ktzmq-libzmq")
include(":ktzmq-cio")
include(":ktzmq-tests")
