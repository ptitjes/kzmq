/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ socket of type [XSUB][Type.XSUB].
 *
 * The behavior of an [XSubscriberSocket] is the same as a [SubscriberSocket],
 * except that you can send subscription/unsubscription messages to the peers.
 *
 * Subscription messages contain a unique frame starting with a '1' byte.
 * Subscription cancellation messages contain a unique frame starting with a '0' byte.
 * Other messages are distributed as is.
 *
 * <br/><table>
 * <tr><th colspan="2">Summary of socket characteristics</th></tr>
 * <tr><td>Compatible peer sockets</td><td>PUB, XPUB</td></tr>
 * <tr><td>Direction</td><td>Unidirectional</td></tr>
 * <tr><td>Send/receive pattern</td><td>Receive messages, send subscriptions</td></tr>
 * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
 * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
 * <tr><td>Action in mute state</td><td>Drop</td></tr>
 * </table><br/>
 */
public interface XSubscriberSocket : Socket, SendSocket, ReceiveSocket
