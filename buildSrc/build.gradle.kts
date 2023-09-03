/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // Workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.atomicfu.plugin)
    implementation(libs.kotlinx.kover.plugin)
    implementation(libs.kotest.plugin)
    implementation(libs.mockmp.plugin)
}
