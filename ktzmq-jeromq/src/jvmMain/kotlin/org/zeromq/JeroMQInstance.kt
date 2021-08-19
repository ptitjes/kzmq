package org.zeromq

import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.channels.SelectableChannel
import kotlin.coroutines.resume

internal const val TRACE = false

internal class JeroMQInstance private constructor(
    private val underlying: ZContext
) : EngineInstance {

    constructor(ioThreads: Int = 1) : this(ZContext(ioThreads))

    override fun createPublisher(): PublisherSocket = wrappingExceptions {
        JeroMQPublisherSocket(this, newSocket(SocketType.PUB))
    }

    override fun createSubscriber(): SubscriberSocket = wrappingExceptions {
        JeroMQSubscriberSocket(this, newSocket(SocketType.SUB))
    }

    private fun newSocket(type: SocketType): ZMQ.Socket = underlying.createSocket(type)

    private val poller = ZPoller(underlying)

    init {
        val pollThread = Thread {
            while (true) {
                poller.poll(-1)
            }
        }
        pollThread.start()
    }

    internal suspend fun suspendUntilEvents(socket: ZMQ.Socket, events: Int) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val handler = object : ZPoller.EventsHandler {
                override fun events(socket: ZMQ.Socket, events: Int): Boolean {
                    poller.unregister(socket)
                    continuation.resume(Unit)
                    return false
                }

                override fun events(channel: SelectableChannel, events: Int): Boolean {
                    return false
                }
            }
            poller.register(socket, handler, events)

            continuation.invokeOnCancellation {
                if (TRACE) println("$socket: cancelling poller registration")
                poller.unregister(socket)
            }
        }
    }
}
