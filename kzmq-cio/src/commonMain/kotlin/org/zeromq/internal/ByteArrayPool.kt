/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import io.ktor.utils.io.pool.*
import org.zeromq.*
import kotlin.native.concurrent.*

private const val BYTE_ARRAY_POOL_SIZE = 4096

internal const val BYTE_BUFFER_ARRAY_LENGTH: Int = 100

@ThreadLocal
internal val ByteArrayPool: ObjectPool<ByteArray> =
    object : DefaultPool<ByteArray>(BYTE_ARRAY_POOL_SIZE) {
        override fun produceInstance(): ByteArray = ByteArray(BYTE_BUFFER_ARRAY_LENGTH)
    }

private var count = 0
private var borrows = 0
private var releases = 0

private const val WARN_COUNT = 2000

internal fun borrowBuffer(): ByteArray {
    count++
    borrows++
//    if (count > WARN_COUNT) println("at borrow - borrowed: $count; totalBorrowed: $borrows; totalReleases: $releases; pool capacity: ${ByteArrayPool.capacity}")
    return ByteArrayPool.borrow()
}

internal fun releaseMessage(message: Message) {
    for (bytes in message.parts) {
        ByteArrayPool.recycle(bytes)
        count--
        releases++
//        if (count > WARN_COUNT) println("at release - borrowed: $count; totalBorrowed: $borrows; totalReleases: $releases; pool capacity: ${ByteArrayPool.capacity}")
    }
}

internal fun displayStats() {
    println("borrowed: $count; totalBorrowed: $borrows; totalReleases: $releases; pool capacity: ${ByteArrayPool.capacity}")
}
