/*
 * Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*

internal class PeerManager(
    override val coroutineContext: CoroutineContext,
    private val transportRegistry: TransportRegistry,
) : CoroutineScope {
    private val peerJobs = JobMap<String>()

    fun bind(endpoint: String, socketInfo: SocketInfo) {
        val transport = transportRegistry.getTransportFor(endpoint)
        peerJobs.add(endpoint) {
            launch { transport.bind(this@PeerManager, socketInfo, endpoint) }
        }
    }

    fun unbind(endpoint: String) = peerJobs.remove(endpoint)

    fun connect(endpoint: String, socketInfo: SocketInfo) {
        val transport = transportRegistry.getTransportFor(endpoint)
        peerJobs.add(endpoint) {
            launch { transport.connect(this@PeerManager, socketInfo, endpoint) }
        }
    }

    fun disconnect(endpoint: String) = peerJobs.remove(endpoint)

    private val _peerEvents = Channel<PeerEvent>()
    val peerEvents: ReceiveChannel<PeerEvent> get() = _peerEvents

    suspend fun notify(event: PeerEvent) {
        logger.d { "Peer event: $event" }
        _peerEvents.send(event)
    }
}
