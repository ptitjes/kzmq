package org.zeromq.internal

internal actual fun log(message: () -> String) {
    println("[${Thread.currentThread().name}] ${message()}")
}
