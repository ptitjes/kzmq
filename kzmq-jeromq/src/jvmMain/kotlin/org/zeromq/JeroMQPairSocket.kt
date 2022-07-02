/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

internal class JeroMQPairSocket internal constructor(
    underlying: ZMQ.Socket,
) : JeroMQSocket(underlying, Type.PAIR), PairSocket
