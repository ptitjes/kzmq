/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.dokka.gradle.*
import java.net.*

plugins {
    id("plugin.library")
    id("plugin.atomicfu")
    id("plugin.kotest")
    id("plugin.mocks")
}

kotlin {
    jvmTargets()
    jsTargets()
    nativeTargets { it.isSupportedByCIO || it.isSupportedByLibzmq }
}

tasks.withType<DokkaTask> {
    dokkaSourceSets {
        named("commonMain") {
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(URL("https://github.com/ptitjes/kzmq/tree/master/kzmq-core/src/commonMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
