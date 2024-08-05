/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlinx.io.bytestring.*
import kotlin.reflect.*

internal fun <T> notImplementedProperty() = NotImplementedPropertyDelegate<T>()

internal class NotImplementedPropertyDelegate<T>() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        TODO("JeroMQ does not implement ${property.name}")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
        TODO("JeroMQ does not implement ${property.name}")
}

internal fun <V, U> KMutableProperty0<V>.converted(into: (V) -> U, from: (U) -> V) =
    MappedPropertyDelegate(this, into, from)

internal class MappedPropertyDelegate<V, U>(
    private val delegate: KMutableProperty0<V>,
    private val into: (V) -> U,
    private val from: (U) -> V,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): U = into(delegate.get())

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: U): Unit = delegate.set(from(value))
}

internal fun KMutableProperty0<ByteArray?>.converted() = converted(
    into = { it?.let { ByteString(it) } },
    from = { it?.toByteArray() }
)
