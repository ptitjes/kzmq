package org.zeromq

import kotlinx.cinterop.COpaquePointer

internal class LibzmqPublisherSocket internal constructor(underlying: COpaquePointer?) :
    LibzmqSocket(underlying), PublisherSocket {
}
