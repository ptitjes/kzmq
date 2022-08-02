/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal.inproc

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.zeromq.internal.*
import kotlin.coroutines.*

internal class InprocEndpointHandler(
    coroutineContext: CoroutineContext,
    endpoint: String,
) : CoroutineScope {

    override val coroutineContext = coroutineContext + CoroutineName("inproc-handler-$endpoint")

    private val refCount = atomic(0)
    private var job: Job? = null

    fun acquire() {
        if (refCount.getAndIncrement() == 0) job = launch { handleEndpointEvents() }
    }

    fun release() {
        if (refCount.decrementAndGet() == 0) job?.cancel()
    }

    fun close() {
        job?.cancel()
    }

    private val endpointEvents = Channel<InprocEndpointEvent>()

    suspend fun notify(event: InprocEndpointEvent) = endpointEvents.send(event)

    private var boundMailbox: Pair<PeerManager, () -> PeerMailbox>? = null
    private val connectedMailboxes = linkedMapOf<PeerMailbox, PeerManager>()
    private val forwardJobs = JobMap<PeerMailbox>()

    private suspend fun handleEndpointEvents() = coroutineScope {
        logger.d { "Starting handler" }
        try {
            while (isActive) {
                val event = endpointEvents.receive()
                logger.d { "Processing event: $event" }
                when (event) {
                    is InprocEndpointEvent.Binding -> {
                        boundMailbox = event.peerManager to event.mailboxFactory
                        connectedMailboxes.forEach { (peerMailbox, peerManager) ->
                            addForwarding(event.peerManager, event.mailboxFactory(), peerManager, peerMailbox)
                        }
                    }

                    is InprocEndpointEvent.Unbinding -> {
                        connectedMailboxes.forEach { (peerMailbox) ->
                            removeForwarding(peerMailbox)
                        }
                    }

                    is InprocEndpointEvent.Connecting -> {
                        connectedMailboxes += event.mailbox to event.peerManager
                        boundMailbox?.let { (peerManager, mailboxFactory) ->
                            addForwarding(peerManager, mailboxFactory(), event.peerManager, event.mailbox)
                        }
                    }

                    is InprocEndpointEvent.Disconnecting -> {
                        connectedMailboxes -= event.mailbox
                        removeForwarding(event.mailbox)
                    }
                }
            }
        } finally {
            forwardJobs.removeAll()
        }
    }

    private fun addForwarding(
        boundPeerManager: PeerManager,
        boundPeerMailbox: PeerMailbox,
        connectedPeerManager: PeerManager,
        connectedPeerMailbox: PeerMailbox,
    ) {
        forwardJobs.add(connectedPeerMailbox) {
            launch {
                logger.d { "Starting forwarding between $boundPeerMailbox and $connectedPeerMailbox" }
                try {
                    setupMailboxForwarding(boundPeerMailbox, connectedPeerMailbox)

                    boundPeerManager.notify(PeerEvent.Kind.ADDITION, boundPeerMailbox)
                    boundPeerManager.notify(PeerEvent.Kind.CONNECTION, boundPeerMailbox)
                    connectedPeerManager.notify(PeerEvent.Kind.CONNECTION, connectedPeerMailbox)

                    awaitCancellation()
                } finally {
                    logger.d { "Stopping forwarding between $boundPeerMailbox and $connectedPeerMailbox" }
                    connectedPeerManager.notify(PeerEvent.Kind.DISCONNECTION, connectedPeerMailbox)
                    boundPeerManager.notify(PeerEvent.Kind.DISCONNECTION, boundPeerMailbox)
                    boundPeerManager.notify(PeerEvent.Kind.REMOVAL, boundPeerMailbox)
                }
            }
        }
    }

    private fun removeForwarding(peerMailbox: PeerMailbox) {
        forwardJobs.remove(peerMailbox)
    }

}

private fun CoroutineScope.setupMailboxForwarding(mailbox1: PeerMailbox, mailbox2: PeerMailbox) {
    launch { forwardChannels(mailbox1.sendChannel, mailbox2.receiveChannel) }
    launch { forwardChannels(mailbox2.sendChannel, mailbox1.receiveChannel) }
}

private suspend fun <T> forwardChannels(incoming: ReceiveChannel<T>, outgoing: SendChannel<T>) =
    coroutineScope { while (isActive) outgoing.send(incoming.receive()) }
