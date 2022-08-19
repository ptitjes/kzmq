/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*

fun KotlinMultiplatformExtension.optIns() {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
        }
    }
}

fun KotlinMultiplatformExtension.jvmTargets() {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
}

fun KotlinMultiplatformExtension.jsTargets() {
    js(IR) {
        nodejs {}
    }
}

fun KotlinMultiplatformExtension.nativeTargets(
    onlyHostTargets: Boolean = false,
) {
    KonanTarget.predefinedTargets

    val targets = if (onlyHostTargets) loadOnlyHostTargets() else loadAllNativeTargets()

    sourceSets {
        val nativeMain by creating {
            findByName("commonMain")?.let { dependsOn(it) }
        }
        val nativeTest by creating {
            findByName("commonTest")?.let { dependsOn(it) }
        }

        targets.forEach { target ->
            getByName("${target.name}Main").dependsOn(nativeMain)
            getByName("${target.name}Test").dependsOn(nativeTest)
        }
    }
}

fun KotlinMultiplatformExtension.loadOnlyHostTargets(): List<KotlinNativeTarget> {
    return when {
        HostManager.hostIsLinux && HostManager.host.architecture == Architecture.X64 -> listOf(linuxX64())

        HostManager.hostIsMac -> when (HostManager.host.architecture) {
            Architecture.ARM64 -> listOf(macosArm64())
            Architecture.X64 -> listOf(macosX64())
            else -> listOf()
        }

        HostManager.hostIsMingw && HostManager.host.architecture == Architecture.X64 -> listOf(mingwX64())

        else -> listOf()
    }
}

private fun KotlinMultiplatformExtension.loadAllNativeTargets() = listOf(
    iosArm32(),
    iosArm64(),
    iosX64(),
    iosSimulatorArm64(),
    watchosArm32(),
    watchosArm64(),
    // watchosX86(), // Not supported by Kotest
    watchosX64(),
    tvosArm64(),
    tvosX64(),
    tvosSimulatorArm64(),
    macosArm64(),
    macosX64(),
    // linuxArm32Hfp(), // Not supported by coroutines
    // linuxArm64(), // Not supported by coroutines
    linuxX64(),
)
