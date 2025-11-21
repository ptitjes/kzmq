/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
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
    val execOutput = project.providers.exec {
        setCommandLine("pkg-config", *args)
        isIgnoreExitValue = true
    }

    val result = execOutput.result.get()
    if (result.exitValue != 0) {
        val errorOutput = execOutput.standardError.asText.get()
        val errorMessage = "Failed to get pkg-config for '${args.joinToString(" ")}': $errorOutput"
        logger.error(errorMessage)
        error(errorMessage)
    }

    return execOutput.standardOutput.asText.get().trim().split("\\s*")
}
