/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import kotlinx.kover.api.*
import org.jetbrains.dokka.gradle.*

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.atomicfu") apply false
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    id("io.kotest.multiplatform") apply false
}

val projectVersion: String by project

subprojects {
    group = "org.zeromq"
    version = projectVersion
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

tasks.withType<DokkaMultiModuleTask> {
    removeChildTasks(
        listOf(
            project(":kzmq-tests"),
            project(":kzmq-tools"),
        )
    )
}

extensions.configure<KoverMergedConfig> {
    enable()

    filters {
        classes {
            excludes += "org.zeromq.tests.utils.*"
        }
        projects {
            excludes += ":kzmq-tools"
        }
    }
}
