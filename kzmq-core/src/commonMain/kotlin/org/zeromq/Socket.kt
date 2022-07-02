/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

interface Socket {
    /**
     * The socket type of this socket.
     */
    val type: Type

    /**
     * Closes this socket.
     */
    fun close()

    /**
     * Creates an endpoint for accepting connections and binds to it.
     *
     * The endpoint argument is a string consisting of two parts as follows: transport ://address. The
     * transport part specifies the underlying transport protocol to use. The meaning of the address
     * part is specific to the underlying transport protocol selected.
     *
     * @param endpoint the endpoint to bind to
     */
    suspend fun bind(endpoint: String)

    /**
     * Unbinds to the endpoint.
     *
     * The endpoint argument is a string consisting of two parts as follows: transport ://address. The
     * transport part specifies the underlying transport protocol to use. The meaning of the address
     * part is specific to the underlying transport protocol selected.
     *
     * @param endpoint the endpoint to unbind from
     */
    suspend fun unbind(endpoint: String)

    /**
     * Connects the socket to an endpoint and then accepts incoming connections on that endpoint.
     *
     * The endpoint is a string consisting of a transport :// followed by an address. The transport
     * specifies the underlying protocol to use. The address specifies the transport-specific address
     * to connect to.
     *
     * @param endpoint the endpoint to connect to
     */
    fun connect(endpoint: String)

    /**
     * Disconnecting a socket from an endpoint.
     *
     * @param endpoint the endpoint to disconnect from
     */
    fun disconnect(endpoint: String)
}
