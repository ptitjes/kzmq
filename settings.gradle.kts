pluginManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ktzmq"

include(":ktzmq-core")
include(":ktzmq-js")
include(":ktzmq-jeromq")
include(":ktzmq-libzmq")
