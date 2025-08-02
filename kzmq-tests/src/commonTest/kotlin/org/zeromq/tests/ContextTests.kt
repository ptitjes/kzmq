/*
 * Copyright (c) 2022-2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests

import de.infix.testBalloon.framework.*
import org.zeromq.*
import kotlin.test.*

val ContextTests by testSuite {
    test("Auto-discover engines") {
        val context = Context()
        assertNotNull(context)
    }
}
