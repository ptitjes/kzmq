/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [PAIR][Type.PAIR].
 * A [PairSocket] can only be connected to a single peer of type [PairSocket] at any one time.
 *
 * No message routing or filtering is performed on messages sent over a [PairSocket].
 *
 * When a [PairSocket] enters the mute state due to having reached the high water mark for the connected peer,
 * or if no peer is connected, then any [send()][SendSocket.send] operations on the socket shall suspend
 * until the peer becomes available for sending; messages are not discarded.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>PAIR</td></tr>
 * <tr><td>Direction</td><td>Bidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Unrestricted</td></tr>
 * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
 * <tr><td>Action in mute state</td><td>Suspends</td></tr>
 * </table><br/>
 *
 * **[PairSocket]s are designed for inter-thread communication across the `inproc` transport
 * and do not implement functionality such as auto-reconnection.
 * [PairSocket]s are considered experimental and may have other missing or broken aspects.**
 */
public interface PairSocket : Socket, SendSocket, ReceiveSocket
