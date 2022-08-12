/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import kotlinx.kover.api.KoverMergedConfig
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

buildscript {
    val kotlinVersion: String by project
    val kotlinxAtomicFuVersion: String by project
    val dokkaVersion: String by project
    val kotlinxKoverVersion: String by project
    val kotestPluginVersion: String by project

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$kotlinxAtomicFuVersion")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
        classpath("org.jetbrains.kotlinx:kover:$kotlinxKoverVersion")

        classpath("io.kotest:kotest-framework-multiplatform-plugin-gradle:$kotestPluginVersion")
    }
}

val projectVersion: String by project

subprojects {
    group = "org.zeromq"
    version = projectVersion

    apply(plugin = "kotlin-multiplatform")
    apply(plugin = "kotlinx-atomicfu")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "kover")
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

apply(plugin = "org.jetbrains.dokka")
apply(plugin = "kover")

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
