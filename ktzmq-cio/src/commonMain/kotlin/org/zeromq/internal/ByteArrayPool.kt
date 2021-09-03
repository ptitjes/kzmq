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

var count = 0

private const val WARN_COUNT = 2000

internal fun borrowBuffer(): ByteArray {
//    count++
//    if (count > WARN_COUNT) println("at borrow - borrowed: $count")
    return ByteArrayPool.borrow()
}

fun releaseMessage(message: Message) {
    for (bytes in message.parts) {
        ByteArrayPool.recycle(bytes)
//        count--
//        if (count > WARN_COUNT) println("at release - borrowed: $count")
    }
}
