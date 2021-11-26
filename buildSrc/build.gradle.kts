/*
 * Copyright (c) 2021 Didier Villevalois and Kzmq contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.*

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

val props = Properties().apply {
    file("../gradle.properties").inputStream().use { load(it) }
}

val kotlinVersion: String? = props.getProperty("kotlinVersion")

dependencies {
    println("Used kotlin version in buildSrc: $kotlinVersion")
    implementation(kotlin("gradle-plugin", kotlinVersion))
}
