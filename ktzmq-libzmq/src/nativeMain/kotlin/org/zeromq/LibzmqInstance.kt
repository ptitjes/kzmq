package org.zeromq

import kotlinx.cinterop.*
import org.zeromq.internal.libzmq.*

internal class LibzmqInstance private constructor(private val underlying: COpaquePointer?) :
    EngineInstance {

    constructor(ioThreads: Int = 1) : this(zmq_ctx_new()) {
        if (zmq_ctx_set(this.underlying, ZMQ_IO_THREADS, ioThreads) != 0) throwNativeError()
    }

    override fun close() {
        if (zmq_ctx_term(underlying) != 0) throwNativeError()
    }

    override fun createPublisher(): PublisherSocket {
        return LibzmqPublisherSocket(zmq_socket(underlying, ZMQ_PUB) ?: throwNativeError())
    }

    override fun createSubscriber(): SubscriberSocket {
        return LibzmqSubscriberSocket(zmq_socket(underlying, ZMQ_SUB) ?: throwNativeError())
    }

    override fun createPair(): PairSocket {
        TODO("Not yet implemented")
    }

    override fun createXPublisher(): XPublisherSocket {
        TODO("Not yet implemented")
    }

    override fun createXSubscriber(): XSubscriberSocket {
        TODO("Not yet implemented")
    }

    override fun createPush(): PushSocket {
        TODO("Not yet implemented")
    }

    override fun createPull(): PullSocket {
        TODO("Not yet implemented")
    }

    override fun createRequest(): RequestSocket {
        TODO("Not yet implemented")
    }

    override fun createReply(): ReplySocket {
        TODO("Not yet implemented")
    }

    override fun createDealer(): DealerSocket {
        TODO("Not yet implemented")
    }

    override fun createRouter(): RouterSocket {
        TODO("Not yet implemented")
    }
}
