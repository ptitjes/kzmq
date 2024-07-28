/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import org.zeromq.internal.zeromqjs.XSubscriber as ZXSubscriber

internal class ZeroMQJsXSubscriberSocket internal constructor(override val underlying: ZXSubscriber = ZXSubscriber()) :
    ZeroMQJsSocket(Type.XSUB),
    SendSocket by ZeroMQJsSendSocket(underlying),
    ReceiveSocket by ZeroMQJsReceiveSocket(underlying),
    XSubscriberSocket
