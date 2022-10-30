/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.*
import org.gradle.api.specs.*
import org.gradle.kotlin.dsl.*
import org.gradle.process.internal.ExecException
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.konan.target.*
import java.io.*

val CI by lazy { !"false".equals(System.getenv("CI") ?: "false", true) }
val SANDBOX by lazy { !"false".equals(System.getenv("SANDBOX") ?: "false", true) }

val Project.isMainHost: Boolean
    get() = HostManager.simpleOsName().equals("${properties["project.mainOS"]}", true)

fun Collection<KotlinTarget>.onlyBuildIf(enabled: Spec<in Task>) {
    forEach {
        it.compilations.all {
            compileKotlinTask.onlyIf(enabled)
        }
    }
}

val org.gradle.api.NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.`jvmMain`: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named<KotlinSourceSet>("jvmMain")

val org.gradle.api.NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.`jsMain`: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named<KotlinSourceSet>("jsMain")

val org.gradle.api.NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.`nativeMain`: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named<KotlinSourceSet>("nativeMain")

val org.gradle.api.NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.`jvmTest`: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named<KotlinSourceSet>("jvmTest")

val org.gradle.api.NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.`jsTest`: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named<KotlinSourceSet>("jsTest")

val org.gradle.api.NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.`nativeTest`: NamedDomainObjectProvider<KotlinSourceSet>
    get() = named<KotlinSourceSet>("nativeTest")

fun Project.pkgConfig(vararg args: String): List<String> {
    val output = ByteArrayOutputStream()
    try {
        project.exec {
            setCommandLine("pkg-config", *args)
            standardOutput = output
        }
        return output.toByteArray().decodeToString().trim().split("\\s*")
    } catch (e: Exception) {
        val standardOutput = output.toByteArray().decodeToString()
        throw IllegalStateException("Failed to get pkg-config for '$args: $standardOutput")
    }
}
