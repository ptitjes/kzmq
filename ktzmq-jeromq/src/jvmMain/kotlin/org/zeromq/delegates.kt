package org.zeromq

import kotlin.reflect.*

internal fun <T> notImplementedProperty() = NotImplementedPropertyDelegate<T>()

internal class NotImplementedPropertyDelegate<T>() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        TODO("JeroMQ does not implement ${property.name}")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
        TODO("JeroMQ does not implement ${property.name}")
}
