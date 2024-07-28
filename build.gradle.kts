/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlinx.kover)
}

val projectVersion: String = libs.versions.project.get()

subprojects {
    group = "org.zeromq"
    version = projectVersion
}

dependencies {
    kover(project(":kzmq-core"))
    kover(project(":kzmq-cio"))
    kover(project(":kzmq-libzmq"))
    kover(project(":kzmq-tests"))
}
