/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Reply as ZReply

internal class ZeroMQJsReplySocket internal constructor(
    override val underlying: ZReply = ZReply(),
) :
    ZeroMQJsSocket(Type.PUB),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    SendSocket by ZeroMQJsSendSocket(underlying),
    ReplySocket {

    override var routingId: String? by underlying::routingId
}
