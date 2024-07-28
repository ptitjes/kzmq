/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlin.time.*

internal class SocketOptions {
    val plainOptions = PlainMechanismOptions()
    val curveOptions = CurveMechanismOptions()

    var receiveQueueSize: Int = 1000
    var sendQueueSize: Int = 1000

    var lingerTimeout: Duration = Duration.INFINITE

    var routingId: ByteArray? = null
}

internal class PlainMechanismOptions {
    var username: String? = null
    var password: String? = null
    var asServer: Boolean = false
}

internal class CurveMechanismOptions {
    var publicKey: ByteArray? = null
    var secretKey: ByteArray? = null
    var serverKey: ByteArray? = null
    var asServer: Boolean = false
}

internal fun SocketOptions.getSelectedSecurityMechanism(): Mechanism {
    if (curveOptions.publicKey != null) return Mechanism.CURVE
    if (plainOptions.username != null) return Mechanism.PLAIN
    return Mechanism.NULL
}
