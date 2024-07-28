/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.target.KonanTarget.*

fun KotlinMultiplatformExtension.jvmTargets() {
    jvm()
}

fun KotlinMultiplatformExtension.jsTargets() {
    js {
        binaries.library()
        useCommonJs()
        nodejs()
    }
}

private val ignoredTargets = setOf(
    LINUX_ARM64,
    MINGW_X64,
    WATCHOS_ARM32, // not supported by Mokkery
)

fun KotlinMultiplatformExtension.nativeTargets(
    predicate: (KonanTarget) -> Boolean,
) {
    KonanTarget.predefinedTargets.values.filter { it !in ignoredTargets }.filter(predicate)
        .forEach { perKonanTargetApplier[it]?.invoke(this) }
}

private val perKonanTargetApplier = mutableMapOf<KonanTarget, KotlinMultiplatformExtension.() -> KotlinNativeTarget>(
    ANDROID_X64 to { androidNativeX64() },
    ANDROID_X86 to { androidNativeX86() },
    ANDROID_ARM32 to { androidNativeArm32() },
    ANDROID_ARM64 to { androidNativeArm64() },
    IOS_ARM64 to { iosArm64() },
    IOS_X64 to { iosX64() },
    IOS_SIMULATOR_ARM64 to { iosSimulatorArm64() },
    WATCHOS_ARM32 to { watchosArm32() },
    WATCHOS_ARM64 to { watchosArm64() },
    WATCHOS_X64 to { watchosX64() },
    WATCHOS_DEVICE_ARM64 to { watchosDeviceArm64() },
    WATCHOS_SIMULATOR_ARM64 to { watchosSimulatorArm64() },
    TVOS_ARM64 to { tvosArm64() },
    TVOS_X64 to { tvosX64() },
    TVOS_SIMULATOR_ARM64 to { tvosSimulatorArm64() },
    MACOS_X64 to { macosX64() },
    MACOS_ARM64 to { macosArm64() },
    LINUX_X64 to { linuxX64() },
    LINUX_ARM64 to { linuxArm64() },
    MINGW_X64 to { mingwX64() },
)

val KonanTarget.buildHost: Family
    get() = when (this) {
        ANDROID_X64,
        ANDROID_X86,
        ANDROID_ARM32,
        ANDROID_ARM64,
        LINUX_ARM64,
        LINUX_X64,
        -> Family.LINUX

        MINGW_X64,
        -> Family.MINGW

        IOS_ARM64,
        IOS_X64,
        IOS_SIMULATOR_ARM64,
        WATCHOS_ARM32,
        WATCHOS_ARM64,
        WATCHOS_X64,
        WATCHOS_SIMULATOR_ARM64,
        WATCHOS_DEVICE_ARM64,
        TVOS_ARM64,
        TVOS_X64,
        TVOS_SIMULATOR_ARM64,
        MACOS_X64,
        MACOS_ARM64,
        -> Family.OSX

        else -> error("Target $this not supported")
    }

val KonanTarget.isSupportedByCIO get() = this.isSupportedByKtorNetwork && this !in cioIgnoredTargets
val KonanTarget.isSupportedByLibzmq get() = this in targetsSupportedByLibzmq
val KonanTarget.isSupportedByKtorNetwork get() = this in targetsSupportedByKtorNetwork

private val cioIgnoredTargets = setOf(
    LINUX_ARM64, // Not supported by Kermit
)

private val targetsSupportedByLibzmq = setOf(
    LINUX_ARM64,
    LINUX_X64,
    MINGW_X64,
    MACOS_ARM64,
    MACOS_X64,
)

private val targetsSupportedByKtorNetwork = setOf(
    LINUX_ARM64,
    LINUX_X64,
    MACOS_ARM64,
    MACOS_X64,
    IOS_ARM64,
    IOS_X64,
    IOS_SIMULATOR_ARM64,
    WATCHOS_ARM32,
    WATCHOS_ARM64,
    WATCHOS_SIMULATOR_ARM64,
    TVOS_ARM64,
    TVOS_X64,
    TVOS_SIMULATOR_ARM64,
)
