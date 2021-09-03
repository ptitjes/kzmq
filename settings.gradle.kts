pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ktzmq"

include(":ktzmq-core")
include(":ktzmq-js")
include(":ktzmq-jeromq")
include(":ktzmq-libzmq")
include(":ktzmq-cio")
include(":ktzmq-tests")
