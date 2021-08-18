package org.zeromq

import kotlinx.cinterop.COpaquePointer
import org.zeromq.internal.libzmq.*

internal class LibzmqInstance private constructor(private val underlying: COpaquePointer?) :
    EngineInstance {

    constructor(ioThreads: Int = 1) : this(zmq_ctx_new()) {
        if (zmq_ctx_set(this.underlying, ZMQ_IO_THREADS, ioThreads) != 0) throwNativeError()
    }

    override fun createPublisher(): PublisherSocket {
        return LibzmqPublisherSocket(zmq_socket(underlying, ZMQ_PUB) ?: throwNativeError())
    }

    override fun createSubscriber(): SubscriberSocket {
        return LibzmqSubscriberSocket(zmq_socket(underlying, ZMQ_SUB) ?: throwNativeError())
    }
}
