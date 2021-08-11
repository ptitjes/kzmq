plugins {
    kotlin("multiplatform") version "1.5.21"
}

group = "org.zeromq"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotlinxCoroutinesVersion = "1.5.1"
val jeromqVersion = "0.5.2"

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

//    js(IR) {
//        nodejs {}
//        binaries.library()
//    }

    val hostOs = System.getProperty("os.name")

    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    hostTarget.apply {
        compilations["main"].cinterops {
            val libzmq by creating {
                when (preset) {
                    presets["macosX64"] -> includeDirs.headerFilterOnly(
                        "/opt/local/include",
                        "/usr/local/include"
                    )
                    presets["linuxX64"] -> includeDirs.headerFilterOnly(
                        "/usr/include",
                        "/usr/include/x86_64-linux-gnu"
                    )
                    presets["mingwX64"] -> includeDirs.headerFilterOnly(mingwPath.resolve("include"))
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.zeromq:jeromq:$jeromqVersion")
            }
        }
        val jvmTest by getting {
        }

//        val jsMain by getting {
//            dependencies {
//                implementation(npm("zeromq", "6.0.0-beta.6", generateExternals = true))
//            }
//        }
//        val jsTest by getting

        val nativeMain by getting
        val nativeTest by getting
    }
}
