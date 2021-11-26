/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.Publisher as ZPublisher

internal class ZeroMQJsPublisherSocket internal constructor(
    override val underlying: ZPublisher = ZPublisher()
) :
    ZeroMQJsSocket(Type.PUB),
    SendSocket by ZeroMQJsSendSocket(underlying),
    PublisherSocket {

    override var conflate: Boolean by underlying::conflate
    override var invertMatching: Boolean by underlying::invertMatching
    override var noDrop: Boolean by underlying::noDrop
}
