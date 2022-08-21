/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.dokka.gradle.*
import java.net.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.atomicfu")
    id("org.jetbrains.kotlinx.kover")
}

val kotlinxAtomicFuVersion: String by project
val kotlinxCoroutinesVersion: String by project

kotlin {
    explicitApi()
    optIns()

    jvmTargets()
    jsTargets()
    nativeTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<DokkaTask> {
    dokkaSourceSets {
        named("commonMain") {
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(URL("https://github.com/ptitjes/kzmq/tree/master/kzmq-core/src/commonMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
