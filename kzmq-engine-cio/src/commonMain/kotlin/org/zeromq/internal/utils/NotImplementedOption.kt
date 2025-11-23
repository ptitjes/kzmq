/*
 * Copyright (c) 2025 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.utils

import kotlin.reflect.*

internal fun <T> notImplementedOption(reason: String = "An option is not implemented.") =
    NotImplementedOptionDelegate<T>(reason)

internal class NotImplementedOptionDelegate<T>(private val reason: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        TODO(reason)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        TODO(reason)
    }
}
