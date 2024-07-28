/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.XPublisher as ZXPublisher

internal class ZeroMQJsXPublisherSocket internal constructor(
    override val underlying: ZXPublisher = ZXPublisher()
) :
    ZeroMQJsSocket(Type.XPUB),
    SendSocket by ZeroMQJsSendSocket(underlying),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    XPublisherSocket {

    override var invertMatching: Boolean by underlying::invertMatching
    override var noDrop: Boolean by underlying::noDrop
//    override var verbose: Boolean by underlying::verbose
//    override var verboser: Boolean by underlying::verboser
    override var manual: Boolean by underlying::manual
    override var welcomeMessage: String? by underlying::welcomeMessage
}
