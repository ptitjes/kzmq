/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Pair as ZPair

internal class ZeroMQJsPairSocket internal constructor(
    override val underlying: ZPair = ZPair(),
) :
    ZeroMQJsSocket(Type.PUB),
    SendSocket by ZeroMQJsSendSocket(underlying),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    PairSocket
