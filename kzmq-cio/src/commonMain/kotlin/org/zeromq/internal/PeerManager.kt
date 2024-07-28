/*
 * Copyright (c) 2022-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.internal

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

internal class PeerManager(
    private val mainScope: CoroutineScope,
    private val lingerScope: CoroutineScope,
    private val transportRegistry: TransportRegistry,
) {

    private val bindingHolders = atomic(mapOf<String, BindingHolder>())
    private val connectionHolders = atomic(mapOf<String, ConnectionHolder>())

    fun close() {
        bindingHolders.getAndUpdate { mapOf() }.forEach { (_, holder) ->
            holder.close()
        }
        connectionHolders.getAndUpdate { mapOf() }.forEach { (_, holder) ->
            holder.close()
        }
    }

    fun bind(address: String, socketInfo: SocketInfo) {
        val transport = transportRegistry.getTransportFor(address)
        val holder = transport.bind(mainScope, lingerScope, this, socketInfo, address)
        bindingHolders.update { it + (address to holder) }
    }

    fun unbind(address: String) {
        bindingHolders.getAndUpdate { it - address }[address]?.close()
    }

    fun connect(address: String, socketInfo: SocketInfo) {
        val transport = transportRegistry.getTransportFor(address)
        val holder = transport.connect(mainScope, lingerScope, this, socketInfo, address)
        connectionHolders.update { it + (address to holder) }
    }

    fun disconnect(address: String) {
        connectionHolders.getAndUpdate { it - address }[address]?.close()
    }

    private val _peerEvents = Channel<PeerEvent>()
    val peerEvents: ReceiveChannel<PeerEvent> get() = _peerEvents

    suspend fun notify(event: PeerEvent) {
        if (!_peerEvents.isClosedForSend) {
            logger.d { "Peer event: $event" }
            yield()
            _peerEvents.send(event)
        }
    }
}
