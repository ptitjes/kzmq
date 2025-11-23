/*
 * Copyright (c) 2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests

import de.infix.testBalloon.framework.core.*

class KzmqTestSession : TestSession(testConfig = DefaultConfiguration.invocation(TestInvocation.CONCURRENT))
