description = "Common tests for engines"

val kotlinxCoroutinesVersion: String by project
val junitVersion: String by project

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

    js(IR) {
        nodejs {}
        binaries.library()
    }

    val hostOs = System.getProperty("os.name")

    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")

                implementation(project(":ktzmq-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":ktzmq-jeromq"))
                implementation(project(":ktzmq-cio"))
            }
        }
        val jvmTest by getting {
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":ktzmq-js"))
            }
        }
        val jsTest by getting {
        }

        val nativeMain by getting {
            dependencies {
                implementation(project(":ktzmq-libzmq"))
            }
        }
        val nativeTest by getting {
        }
    }
}
