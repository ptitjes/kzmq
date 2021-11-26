pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    }
}

rootProject.name = "ktzmq"

include(":ktzmq-core")
include(":ktzmq-js")
include(":ktzmq-jeromq")
include(":ktzmq-libzmq")
include(":ktzmq-cio")
include(":ktzmq-tests")
