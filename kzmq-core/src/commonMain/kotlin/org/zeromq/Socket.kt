/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket.
 */
public interface Socket : AutoCloseable {
    /**
     * The socket type of this socket.
     */
    public val type: Type

    /**
     * Closes this socket.
     */
    override fun close()

    /**
     * Creates an endpoint for accepting connections and binds to it.
     *
     * The [endpoint] argument is a string consisting of two parts as follows: `transport://address`.
     * The transport part specifies the underlying transport protocol to use.
     * The meaning of the address part is specific to the underlying transport protocol selected.
     *
     * @param endpoint the endpoint to bind to
     */
    public suspend fun bind(endpoint: String)

    /**
     * Unbinds to the endpoint.
     *
     * The [endpoint] argument is a string consisting of two parts as follows: `transport://address`.
     * The transport part specifies the underlying transport protocol to use.
     * The meaning of the address part is specific to the underlying transport protocol selected.
     *
     * @param endpoint the endpoint to unbind from
     */
    public suspend fun unbind(endpoint: String)

    /**
     * Connects the socket to an endpoint and then accepts incoming connections on that endpoint.
     *
     * The [endpoint] argument is a string consisting of two parts as follows: `transport://address`.
     * The transport part specifies the underlying transport protocol to use.
     * The meaning of the address part is specific to the underlying transport protocol selected.
     *
     * @param endpoint the endpoint to connect to
     */
    public fun connect(endpoint: String)

    /**
     * Disconnects a socket from an endpoint.
     *
     * The [endpoint] argument is a string consisting of two parts as follows: `transport://address`.
     * The transport part specifies the underlying transport protocol to use.
     * The meaning of the address part is specific to the underlying transport protocol selected.
     *
     * @param endpoint the endpoint to disconnect from
     */
    public fun disconnect(endpoint: String)
}
