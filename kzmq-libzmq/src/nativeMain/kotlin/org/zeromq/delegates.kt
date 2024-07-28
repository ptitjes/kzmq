/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalForeignApi::class)

package org.zeromq

import kotlinx.cinterop.*
import org.zeromq.internal.libzmq.*
import platform.posix.*
import kotlin.reflect.*

internal fun <V : CPrimitiveVar, T> socketOption(
    socket: COpaquePointer?,
    option: Int,
    converter: PrimitiveConverter<V, T>,
) = PrimitiveSocketOptionDelegate<V, T>(socket, option, converter)

internal class PrimitiveSocketOptionDelegate<V : CPrimitiveVar, T>(
    private val socket: COpaquePointer?,
    private val option: Int,
    private val converter: PrimitiveConverter<V, T>,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = memScoped {
        val primitiveVar = converter.instantiate(this)
        val sizeVar = alloc<size_tVar>()
        zmq_getsockopt(socket, option, primitiveVar.ptr, sizeVar.ptr)
        return converter.getValue(primitiveVar)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = memScoped {
        val primitiveVar = converter.instantiate(this)
        converter.setValue(primitiveVar, value)
        zmq_setsockopt(socket, option, primitiveVar.ptr, sizeOf<BooleanVar>().toULong())
    }
}

internal interface PrimitiveConverter<V : CPrimitiveVar, T> {
    fun instantiate(placement: NativePlacement): V
    fun setValue(variable: V, value: T)
    fun getValue(variable: V): T
}

internal val booleanConverter = object : PrimitiveConverter<BooleanVar, Boolean> {
    override fun instantiate(placement: NativePlacement): BooleanVar = placement.alloc()

    override fun setValue(variable: BooleanVar, value: Boolean) {
        variable.value = value
    }

    override fun getValue(variable: BooleanVar): Boolean = variable.value
}

internal val intConverter = object : PrimitiveConverter<IntVar, Int> {
    override fun instantiate(placement: NativePlacement): IntVar = placement.alloc()

    override fun setValue(variable: IntVar, value: Int) {
        variable.value = value
    }

    override fun getValue(variable: IntVar): Int = variable.value
}
