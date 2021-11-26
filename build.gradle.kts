buildscript {
    val kotlinVersion: String by project
    val kotlinxAtomicFuVersion: String by project
    val dokkaVersion: String by project
    val kotestPluginVersion: String by project

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$kotlinxAtomicFuVersion")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")

        classpath("io.kotest:kotest-framework-multiplatform-plugin-gradle:$kotestPluginVersion")
    }
}

val projectVersion: String by project

subprojects {
    group = "org.zeromq"
    version = projectVersion

    apply(plugin = "kotlin-multiplatform")
    apply(plugin = "kotlinx-atomicfu")
    apply(plugin = "org.jetbrains.dokka")
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>() {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
