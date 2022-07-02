/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import mu.*

internal val logger = KotlinLogging.logger {}

internal fun KLogger.d(msg: () -> Any?) = this.debug(msg)
internal fun KLogger.t(msg: () -> Any?) = this.trace(msg)
