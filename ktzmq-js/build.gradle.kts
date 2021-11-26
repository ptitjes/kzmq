val kotlinxCoroutinesVersion: String by project
val jeromqVersion: String by project

kotlin {
    js(IR) {
        nodejs {}
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":ktzmq-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation(npm("zeromq", "6.0.0-beta.6"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
