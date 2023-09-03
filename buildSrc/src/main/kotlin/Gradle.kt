/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.accessors.dm.*
import org.gradle.api.*
import org.gradle.api.specs.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.konan.target.*
import java.io.*

val CI by lazy { !"false".equals(System.getenv("CI") ?: "false", true) }
val SANDBOX by lazy { !"false".equals(System.getenv("SANDBOX") ?: "false", true) }

val Project.libs get() = project.extensions.getByName("libs") as LibrariesForLibs

val Project.isMainHost: Boolean
    get() = HostManager.simpleOsName().equals("${properties["project.mainOS"]}", true)

fun Collection<KotlinTarget>.onlyBuildIf(enabled: Spec<in Task>) {
    forEach {
        it.compilations.all {
            compileTaskProvider.get().onlyIf(enabled)
        }
    }
}

val NamedDomainObjectContainer<KotlinSourceSet>.jvmMain get() = named<KotlinSourceSet>("jvmMain")
val NamedDomainObjectContainer<KotlinSourceSet>.jsMain get() = named<KotlinSourceSet>("jsMain")
val NamedDomainObjectContainer<KotlinSourceSet>.nativeMain get() = named<KotlinSourceSet>("nativeMain")
val NamedDomainObjectContainer<KotlinSourceSet>.jvmTest get() = named<KotlinSourceSet>("jvmTest")
val NamedDomainObjectContainer<KotlinSourceSet>.jsTest get() = named<KotlinSourceSet>("jsTest")
val NamedDomainObjectContainer<KotlinSourceSet>.nativeTest get() = named<KotlinSourceSet>("nativeTest")

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
