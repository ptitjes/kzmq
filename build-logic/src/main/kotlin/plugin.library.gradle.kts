/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("plugin.common")
    id("org.jetbrains.dokka")
}

kotlin {
    explicitApi()
}

dokka {
    dokkaSourceSets {
        all {
            sourceLink {
                localDirectory.set(file("src/$name/kotlin"))
                remoteUrl("https://github.com/ptitjes/kzmq/tree/master/${project.name}/src/$name/kotlin")
                remoteLineSuffix.set("#L")
            }
        }
    }
}
