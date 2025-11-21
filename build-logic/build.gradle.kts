/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>): Dependency =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    implementation(plugin(libs.plugins.kotlin.multiplatform))
    implementation(plugin(libs.plugins.kotlin.atomicfu))
    implementation(plugin(libs.plugins.kotlinx.kover))
    implementation(plugin(libs.plugins.kotlin.dokka))
    implementation(plugin(libs.plugins.testBalloon))
    implementation(plugin(libs.plugins.mokkery))
    implementation(plugin(libs.plugins.maven.publish))
}
