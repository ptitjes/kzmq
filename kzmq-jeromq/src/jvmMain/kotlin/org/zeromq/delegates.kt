/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.reflect.*

internal fun <T> notImplementedProperty() = NotImplementedPropertyDelegate<T>()

internal class NotImplementedPropertyDelegate<T>() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        TODO("JeroMQ does not implement ${property.name}")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
        TODO("JeroMQ does not implement ${property.name}")
}
