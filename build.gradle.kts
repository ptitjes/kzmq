/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.dokka.gradle.*

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    alias(libs.plugins.dokka)
    id("org.jetbrains.kotlinx.kover")
}

val projectVersion: String = libs.versions.project.get()

subprojects {
    group = "org.zeromq"
    version = projectVersion
}

tasks.withType<DokkaMultiModuleTask> {
    removeChildTasks(
        listOf(
            project(":kzmq-tests"),
            project(":kzmq-tools"),
        )
    )
}

koverReport {
    defaults {
        filters {
            excludes {
                packages("org.zeromq.tests.utils")
            }
        }
    }
}
