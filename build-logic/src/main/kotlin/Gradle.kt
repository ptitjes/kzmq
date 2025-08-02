/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.specs.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.konan.target.*

val CI by lazy { !"false".equals(System.getenv("CI") ?: "false", true) }
val SANDBOX by lazy { !"false".equals(System.getenv("SANDBOX") ?: "false", true) }

internal val Project.libs get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.getLibrary(alias: String) = findLibrary(alias).get()

val Project.isMainHost: Boolean
    get() = HostManager.simpleOsName().equals("${properties["project.mainOS"]}", true)

fun Collection<KotlinTarget>.onlyBuildIf(enabled: Spec<in Task>) {
    forEach {
        it.compilations.all {
            compileTaskProvider.get().onlyIf(enabled)
        }
    }
}

fun Project.pkgConfig(vararg args: String): List<String> {
    val output = project.providers.exec {
        isIgnoreExitValue = true
        setCommandLine("pkg-config", *args)
    }

    if (output.result.get().exitValue != 0) {
        val errorOutput = output.standardError.asText.get()
        error("Failed to run pkg-config for '${args.joinToString(" ")}': $errorOutput")
    }

    return output.standardOutput.asText.get().trim().split("\\s*")
}
