/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import org.zeromq.*

class ContextTests : FunSpec({

    test("Auto-discover engines") {
        val context = Context()
        context shouldNotBe null
    }
})
