/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests

import de.infix.testBalloon.framework.core.*
import io.kotest.matchers.*
import org.zeromq.*

val ContextTests by testSuite {

    test("Auto-discover engines") {
        val context = Context()
        context shouldNotBe null
    }
}
