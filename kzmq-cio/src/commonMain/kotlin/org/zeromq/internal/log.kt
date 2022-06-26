/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import mu.*

internal val logger = KotlinLogging.logger {}

fun KLogger.d(msg: () -> Any?) = this.debug(msg)
fun KLogger.t(msg: () -> Any?) = this.trace(msg)
