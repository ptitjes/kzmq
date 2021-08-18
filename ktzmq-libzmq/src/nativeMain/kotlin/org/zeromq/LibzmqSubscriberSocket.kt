package org.zeromq

import kotlinx.cinterop.COpaquePointer

internal class LibzmqSubscriberSocket internal constructor(underlying: COpaquePointer?) :
    LibzmqSocket(underlying), SubscriberSocket {
}
