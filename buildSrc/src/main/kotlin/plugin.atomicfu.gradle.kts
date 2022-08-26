/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("UNUSED_VARIABLE")

import org.gradle.kotlin.dsl.*

plugins {
    id("plugin.common")
    kotlin("plugin.atomicfu")
}

kotlin {
    sourceSets {
        val kotlinxAtomicFuVersion: String by project

        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVersion")
            }
        }
    }
}
