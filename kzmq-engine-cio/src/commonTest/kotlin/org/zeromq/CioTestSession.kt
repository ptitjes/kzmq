/*
 * Copyright (c) 2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import de.infix.testBalloon.framework.core.*

class CioTestSession : TestSession(testConfig = DefaultConfiguration.invocation(TestInvocation.CONCURRENT))
