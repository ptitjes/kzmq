buildscript {
    val kotlinVersion: String by project
    val kotlinxAtomicFuVersion: String by project
    val dokkaVersion: String by project

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$kotlinxAtomicFuVersion")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
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

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>() {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
