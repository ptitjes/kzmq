package org.zeromq

import kotlinx.cinterop.COpaquePointer
import libzmq.ZMQ_IO_THREADS
import libzmq.zmq_ctx_new
import libzmq.zmq_ctx_set
import libzmq.zmq_socket

class LibzmqContext private constructor(private val underlying: COpaquePointer?) : Context {

    constructor(ioThreads: Int = 1) : this(zmq_ctx_new()) {
        if (zmq_ctx_set(this.underlying, ZMQ_IO_THREADS, ioThreads) != 0) throwNativeError()
    }

    override fun createSocket(type: Type): Socket {
        return LibzmqSocket(zmq_socket(underlying, type.type) ?: throwNativeError())
    }
}
