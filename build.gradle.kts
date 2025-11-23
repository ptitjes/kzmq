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
    group = "io.github.ptitjes"
    version = projectVersion
}

dependencies {
    kover(project(":kzmq-core"))
    kover(project(":kzmq-engine-cio"))
    kover(project(":kzmq-engine-jeromq"))
    kover(project(":kzmq-engine-libzmq"))
    kover(project(":kzmq-engine-zeromqjs"))
    kover(project(":kzmq-tests"))
}
