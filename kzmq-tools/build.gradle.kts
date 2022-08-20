import org.jetbrains.kotlin.gradle.plugin.mpp.*

/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform")
    application
}

val kotlinxCoroutinesVersion: String by project
val kotlinxCliVersion: String by project

kotlin {
    optIns()

    jvmTargets()
    nativeTargets(onlyHostTargets = true)

    jvm {
        withJava()
    }

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.apply {
            binaries {
                executable("throughput") { entryPoint = "org.zeromq.tools.main" }
            }
            compilations["main"].enableEndorsedLibs = false
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kzmq-core"))
                implementation(project(":kzmq-cio"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":kzmq-jeromq"))
            }
        }
    }
}

application {
    mainClass.set("org.zeromq.tools.ThroughputKt")
}
