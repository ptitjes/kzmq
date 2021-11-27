/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal actual fun log(message: () -> String) {
//    println("[${Thread.currentThread().name}] ${message()}")
}
