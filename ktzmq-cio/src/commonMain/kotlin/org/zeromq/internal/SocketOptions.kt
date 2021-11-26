/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

internal class SocketOptions {
    var receiveQueueSize: Int = 1000
    var sendQueueSize: Int = 1000
}
