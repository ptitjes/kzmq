pluginManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ktzmq"

include(":ktzmq-core")
include(":ktzmq-js")
include(":ktzmq-jvm-jeromq")
include(":ktzmq-native-libzmq")
